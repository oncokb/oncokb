package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mskcc.cbio.oncokb.api.pub.v1.AnnotationsApiController;
import org.mskcc.cbio.oncokb.api.pub.v1.GermlineAnnotationsApiController;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.service.CancerGeneService;
import org.mskcc.cbio.oncokb.service.GermlineAnnotationService;
import org.mskcc.cbio.oncokb.service.SomaticAnnotationService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Asserts that each MCP annotation tool returns exactly the REST GET endpoint's response body (plus
 * the MCP-only {@code oncokbLinks} object) for representative somatic mutation, copy number,
 * structural variant and germline queries. Both sides run through MockMvc with the real
 * controllers and services, so this also covers the cancer hotspot annotation (hotspot flag and
 * hotspot-derived mutation effect, oncokb/oncokb#4141) that the shared services now produce.
 * <p>
 * Like the core module's tests, this needs a MySQL database loaded with the OncoKB data dump,
 * passed as {@code -Djdbc.url=... -Djdbc.username=... -Djdbc.password=...} (see
 * .github/workflows/unit-test.yml). Genomic-change and HGVS cases also call Genome Nexus. Without
 * {@code jdbc.url} the tests are skipped rather than failed.
 */
public class McpRestParityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static MockMvc mvc;

    @BeforeClass
    public static void setUp() {
        Assume.assumeTrue("jdbc.url is not set; skipping MCP/REST parity tests that need the OncoKB database",
            System.getProperty("jdbc.url") != null && !System.getProperty("jdbc.url").trim().isEmpty());

        CacheFetcher cacheFetcher = new CacheFetcher();
        SomaticAnnotationService somatic = new SomaticAnnotationService();
        ReflectionTestUtils.setField(somatic, "cacheFetcher", cacheFetcher);
        GermlineAnnotationService germline = new GermlineAnnotationService();
        ReflectionTestUtils.setField(germline, "cacheFetcher", cacheFetcher);
        CancerGeneService cancerGenes = new CancerGeneService();
        ReflectionTestUtils.setField(cancerGenes, "cacheFetcher", cacheFetcher);

        AnnotationsApiController restSomatic = new AnnotationsApiController();
        ReflectionTestUtils.setField(restSomatic, "somaticAnnotationService", somatic);
        GermlineAnnotationsApiController restGermline = new GermlineAnnotationsApiController();
        ReflectionTestUtils.setField(restGermline, "germlineAnnotationService", germline);

        McpToolRegistry tools = new McpToolRegistry();
        tools.cacheFetcher = cacheFetcher;
        tools.somaticAnnotationService = somatic;
        tools.germlineAnnotationService = germline;
        tools.cancerGeneService = cancerGenes;
        tools.registerTools();
        McpController mcp = new McpController();
        mcp.toolRegistry = tools;
        mcp.resourceRegistry = new McpResourceRegistry();

        mvc = MockMvcBuilders.standaloneSetup(restSomatic, restGermline, mcp).build();
    }

    @Test
    public void somaticProteinChangeHotspot() throws Exception {
        // BRAF V600E: curated, and a single-residue cancer hotspot.
        JsonNode rest = assertParity(
            get("/api/v1/annotate/mutations/byProteinChange").param("hugoSymbol", "BRAF").param("alteration", "V600E").param("tumorType", "Melanoma"),
            "annotate_mutations_byProteinChange", "{\"hugoSymbol\":\"BRAF\",\"alteration\":\"V600E\",\"tumorType\":\"Melanoma\"}");
        assertTrue(rest.get("geneExist").asBoolean());
        assertTrue(rest.get("hotspot").asBoolean());
        assertEquals("Oncogenic", rest.get("oncogenic").asText());
    }

    @Test
    public void somaticProteinChangeByEntrezGeneId() throws Exception {
        assertParity(
            get("/api/v1/annotate/mutations/byProteinChange").param("entrezGeneId", "3845").param("alteration", "G12C").param("tumorType", "NSCLC"),
            "annotate_mutations_byProteinChange", "{\"entrezGeneId\":3845,\"alteration\":\"G12C\",\"tumorType\":\"NSCLC\"}");
    }

    @Test
    public void somaticProteinChangeUncuratedHotspotResidue() throws Exception {
        // A non-curated substitution at the BRAF V600 hotspot residue, where #4141 can fill in the
        // mutation effect from the hotspot. Whatever it yields, MCP and REST must agree.
        JsonNode rest = assertParity(
            get("/api/v1/annotate/mutations/byProteinChange").param("hugoSymbol", "BRAF").param("alteration", "V600W"),
            "annotate_mutations_byProteinChange", "{\"hugoSymbol\":\"BRAF\",\"alteration\":\"V600W\"}");
        assertTrue(rest.get("hotspot").asBoolean());
    }

    @Test
    public void somaticProteinChangeNonHotspot() throws Exception {
        JsonNode rest = assertParity(
            get("/api/v1/annotate/mutations/byProteinChange").param("hugoSymbol", "AKT1").param("alteration", "E17*"),
            "annotate_mutations_byProteinChange", "{\"hugoSymbol\":\"AKT1\",\"alteration\":\"E17*\"}");
        assertFalse(rest.get("hotspot").asBoolean());
    }

    @Test
    public void somaticGenomicChange() throws Exception {
        JsonNode rest = assertParity(
            get("/api/v1/annotate/mutations/byGenomicChange").param("genomicLocation", "7,140453136,140453136,A,T").param("tumorType", "Melanoma"),
            "annotate_mutations_byGenomicChange", "{\"genomicLocation\":\"7,140453136,140453136,A,T\",\"tumorType\":\"Melanoma\"}");
        assertTrue(rest.get("hotspot").asBoolean());
    }

    @Test
    public void somaticHgvsg() throws Exception {
        assertParity(
            get("/api/v1/annotate/mutations/byHGVSg").param("hgvsg", "7:g.140453136A>T").param("tumorType", "Melanoma"),
            "annotate_mutations_byHGVSg", "{\"hgvsg\":\"7:g.140453136A>T\",\"tumorType\":\"Melanoma\"}");
    }

    @Test
    public void copyNumberAlteration() throws Exception {
        JsonNode rest = assertParity(
            get("/api/v1/annotate/copyNumberAlterations").param("hugoSymbol", "ERBB2").param("copyNameAlterationType", "AMPLIFICATION").param("tumorType", "Breast Cancer"),
            "annotate_copyNumberAlterations", "{\"hugoSymbol\":\"ERBB2\",\"copyNameAlterationType\":\"AMPLIFICATION\",\"tumorType\":\"Breast Cancer\"}");
        assertTrue(rest.get("geneExist").asBoolean());
    }

    @Test
    public void structuralVariantFusion() throws Exception {
        JsonNode rest = assertParity(
            get("/api/v1/annotate/structuralVariants").param("hugoSymbolA", "BCR").param("hugoSymbolB", "ABL1")
                .param("structuralVariantType", "FUSION").param("isFunctionalFusion", "true").param("tumorType", "CML"),
            "annotate_structuralVariants", "{\"hugoSymbolA\":\"BCR\",\"hugoSymbolB\":\"ABL1\",\"structuralVariantType\":\"FUSION\",\"isFunctionalFusion\":true,\"tumorType\":\"CML\"}");
        assertTrue(rest.get("geneExist").asBoolean());
    }

    @Test
    public void germlineGenomicChange() throws Exception {
        assertParity(
            get("/api/v1/annotate/germline/mutations/byGenomicChange").param("genomicLocation", "17,41276045,41276046,CT,-").param("tumorType", "Breast Cancer"),
            "annotate_germline_mutations_byGenomicChange", "{\"genomicLocation\":\"17,41276045,41276046,CT,-\",\"tumorType\":\"Breast Cancer\"}");
    }

    @Test
    public void germlineHgvsg() throws Exception {
        assertParity(
            get("/api/v1/annotate/germline/mutations/byHGVSg").param("hgvsg", "17:g.41276045_41276046del"),
            "annotate_germline_mutations_byHGVSg", "{\"hgvsg\":\"17:g.41276045_41276046del\"}");
    }

    /**
     * Calls the REST endpoint and the MCP tool, asserts the MCP structuredContent equals the REST
     * body once the MCP-only link object is removed, and returns the REST body.
     */
    private static JsonNode assertParity(MockHttpServletRequestBuilder restRequest, String tool, String arguments) throws Exception {
        MockHttpServletResponse restResponse = mvc.perform(restRequest.servletPath("/api/v1")).andReturn().getResponse();
        assertEquals(restResponse.getContentAsString(), 200, restResponse.getStatus());
        JsonNode rest = MAPPER.readTree(restResponse.getContentAsByteArray());

        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"" + tool + "\",\"arguments\":" + arguments + "}}";
        MockHttpServletResponse mcpResponse = mvc.perform(post("/api/v1/mcp").servletPath("/api/v1")
            .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse();
        assertEquals(200, mcpResponse.getStatus());
        JsonNode result = MAPPER.readTree(mcpResponse.getContentAsByteArray()).get("result");
        assertFalse(result.toString(), result.get("isError").asBoolean());
        assertEquals("the payload is sent once", 1, result.get("content").size());

        ObjectNode structured = (ObjectNode) result.get("structuredContent").deepCopy();
        assertTrue("oncokbLinks is added to annotation results", structured.get(McpToolRegistry.LINKS_FIELD).isObject());
        structured.remove(McpToolRegistry.LINKS_FIELD);
        assertEquals(tool, rest, structured);
        return rest;
    }
}
