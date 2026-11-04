package org.mskcc.cbio.oncokb.api.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mskcc.cbio.oncokb.apiModels.GeneticType;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.model.CopyNumberAlterationType;
import org.mskcc.cbio.oncokb.model.InheritanceMechanism;
import org.mskcc.cbio.oncokb.model.GermlineIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.SomaticIndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.StructuralVariantType;
import org.mskcc.cbio.oncokb.service.CancerGeneService;
import org.mskcc.cbio.oncokb.service.GermlineAnnotationService;
import org.mskcc.cbio.oncokb.service.SomaticAnnotationService;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The MCP tools. There is one tool for each {@code @PublicApi} GET endpoint of the REST API
 * (plain-text .txt variants excluded), and each tool calls the same service the REST controller
 * uses, so results match REST.
 * <p>
 * When a {@code @PublicApi} endpoint is added or changed, update the matching tool here.
 */
@Component
public class McpToolRegistry {
    // Parameter descriptions, copied from the @ApiParam text of the REST endpoints.
    private static final String HUGO_SYMBOL = "The gene symbol used in Human Genome Organisation. Example: BRAF";
    private static final String ENTREZ_GENE_ID = "The entrez gene ID. (Higher priority than hugoSymbol). Example: 673";
    private static final String REFERENCE_GENOME = "Reference genome, either GRCh37 or GRCh38. The default is GRCh37";
    private static final String TUMOR_TYPE = "OncoTree(http://oncotree.info) tumor type name. The field supports OncoTree Code, OncoTree Name and OncoTree Main type. Example: Melanoma";
    private static final String EVIDENCE_TYPE = "DEPRECATED. We do not recommend using this parameter and it will eventually be removed.";
    private static final String GENOMIC_LOCATION = "Genomic location following TCGA MAF format. Example: 7,140453136,140453136,A,T";
    private static final String HGVSG = "HGVS genomic format following HGVS nomenclature. Example: 7:g.140453136A>T";
    private static final String HGVSC = "HGVS cDNA format following HGVS nomenclature. Example: EGFR:c.2369C>T";
    private static final String VERSION = "The data version";
    private static final String DEFAULT_REFERENCE_GENOME = "GRCh37";
    // Name of the field added to annotation results. It is not a REST response field.
    static final String LINKS_FIELD = "oncokbLinks";
    private static final String LINKS_NOTE = " The result adds an " + LINKS_FIELD + " object with links to the oncokb.org gene, alteration, and cancer type pages; share them with the user so they can read the full curation.";

    // Built the same way as Spring MVC's Jackson converter, so results match REST responses.
    private final ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();

    @Autowired
    CacheFetcher cacheFetcher;

    @Autowired
    SomaticAnnotationService somaticAnnotationService;

    @Autowired
    GermlineAnnotationService germlineAnnotationService;

    @Autowired
    CancerGeneService cancerGeneService;

    // Keyed by tool name, in the order tools/list returns them.
    private Map<String, McpTool> tools;

    @PostConstruct
    void registerTools() {
        Map<String, McpTool> registered = new LinkedHashMap<>();

        // GET /annotate/mutations/byProteinChange
        add(registered, "annotate_mutations_byProteinChange", "Annotate mutation by protein change." + LINKS_NOTE,
            McpSchema.object()
                .string("hugoSymbol", HUGO_SYMBOL)
                .integer("entrezGeneId", ENTREZ_GENE_ID)
                .string("alteration", "Protein Change. Example: V600E")
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .stringEnum("consequence", "Consequence. Example: missense_variant",
                    "feature_truncation", "frameshift_variant", "inframe_deletion", "inframe_insertion", "start_lost",
                    "missense_variant", "splice_region_variant", "stop_gained", "synonymous_variant", "intron_variant")
                .integer("proteinStart", "Protein Start. Example: 600")
                .integer("proteinEnd", "Protein End. Example: 600")
                .string("tumorType", TUMOR_TYPE)
                .string("evidenceType", EVIDENCE_TYPE),
            args -> withLinks(somaticAnnotationService.annotateMutationByProteinChange(
                args.string("hugoSymbol"),
                args.integer("entrezGeneId"),
                args.string("alteration"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("consequence"),
                args.integer("proteinStart"),
                args.integer("proteinEnd"),
                args.string("tumorType"),
                args.string("evidenceType")), GeneticType.SOMATIC));

        // GET /annotate/mutations/byGenomicChange
        add(registered, "annotate_mutations_byGenomicChange", "Annotate mutation by genomic change." + LINKS_NOTE,
            McpSchema.object()
                .string("genomicLocation", GENOMIC_LOCATION)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .string("evidenceType", EVIDENCE_TYPE)
                .required("genomicLocation"),
            args -> withLinks(somaticAnnotationService.annotateMutationByGenomicChange(
                args.requiredString("genomicLocation"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType"),
                args.string("evidenceType")), GeneticType.SOMATIC));

        // GET /annotate/mutations/byHGVSg
        add(registered, "annotate_mutations_byHGVSg", "Annotate mutation by HGVSg." + LINKS_NOTE,
            McpSchema.object()
                .string("hgvsg", HGVSG)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .string("evidenceType", EVIDENCE_TYPE)
                .required("hgvsg"),
            args -> withLinks(somaticAnnotationService.annotateMutationByHGVSg(
                args.requiredString("hgvsg"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType"),
                args.string("evidenceType")), GeneticType.SOMATIC));

        // GET /annotate/mutations/byHGVSc
        add(registered, "annotate_mutations_byHGVSc", "Annotate mutation by HGVSc." + LINKS_NOTE,
            McpSchema.object()
                .string("hgvsc", HGVSC)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .required("hgvsc"),
            args -> withLinks(somaticAnnotationService.annotateMutationByHGVSc(
                args.requiredString("hgvsc"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType")), GeneticType.SOMATIC));

        // GET /annotate/copyNumberAlterations
        add(registered, "annotate_copyNumberAlterations", "Annotate copy number alteration." + LINKS_NOTE,
            McpSchema.object()
                .string("hugoSymbol", HUGO_SYMBOL)
                .integer("entrezGeneId", ENTREZ_GENE_ID)
                // The misspelled name matches the REST query parameter.
                .stringEnum("copyNameAlterationType", "Copy number alteration type", CopyNumberAlterationType.class)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .string("evidenceType", EVIDENCE_TYPE)
                .required("copyNameAlterationType"),
            args -> withLinks(somaticAnnotationService.annotateCopyNumberAlteration(
                args.string("hugoSymbol"),
                args.integer("entrezGeneId"),
                args.requiredEnum("copyNameAlterationType", CopyNumberAlterationType.class),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType"),
                args.string("evidenceType")), GeneticType.SOMATIC));

        // GET /annotate/structuralVariants
        add(registered, "annotate_structuralVariants", "Annotate structural variant." + LINKS_NOTE,
            McpSchema.object()
                .string("hugoSymbolA", "The gene symbol A used in Human Genome Organisation. Example: ABL1")
                .integer("entrezGeneIdA", "The entrez gene ID A. (Higher priority than hugoSymbolA) Example: 25")
                .string("hugoSymbolB", "The gene symbol B used in Human Genome Organisation.Example: BCR ")
                .integer("entrezGeneIdB", "The entrez gene ID B. (Higher priority than hugoSymbolB) Example: 613")
                .stringEnum("structuralVariantType", "Structural variant type", StructuralVariantType.class)
                .bool("isFunctionalFusion", "Whether is functional fusion", false)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .string("evidenceType", EVIDENCE_TYPE)
                .required("structuralVariantType"),
            args -> withLinks(somaticAnnotationService.annotateStructuralVariant(
                args.string("hugoSymbolA"),
                args.integer("entrezGeneIdA"),
                args.string("hugoSymbolB"),
                args.integer("entrezGeneIdB"),
                args.requiredEnum("structuralVariantType", StructuralVariantType.class),
                args.bool("isFunctionalFusion", false),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType"),
                args.string("evidenceType")), GeneticType.SOMATIC));

        // GET /annotate/germline/mutations/byGenomicChange
        add(registered, "annotate_germline_mutations_byGenomicChange", "Annotate germline mutation by genomic change." + LINKS_NOTE,
            McpSchema.object()
                .string("genomicLocation", GENOMIC_LOCATION)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .required("genomicLocation"),
            args -> withLinks(germlineAnnotationService.annotateMutationByGenomicChange(
                args.requiredString("genomicLocation"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType")), GeneticType.GERMLINE));

        // GET /annotate/germline/mutations/byHGVSg
        add(registered, "annotate_germline_mutations_byHGVSg", "Annotate germline mutation by HGVSg." + LINKS_NOTE,
            McpSchema.object()
                .string("hgvsg", HGVSG)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .required("hgvsg"),
            args -> withLinks(germlineAnnotationService.annotateMutationByHGVSg(
                args.requiredString("hgvsg"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType")), GeneticType.GERMLINE));

        // GET /annotate/germline/mutations/byHGVSc
        add(registered, "annotate_germline_mutations_byHGVSc", "Annotate germline mutation by HGVSc." + LINKS_NOTE,
            McpSchema.object()
                .string("hgvsc", HGVSC)
                .string("referenceGenome", REFERENCE_GENOME, DEFAULT_REFERENCE_GENOME)
                .string("tumorType", TUMOR_TYPE)
                .stringEnumArray("inheritanceMechanisms", "List of inheritance mechanisms used to filter the returned genomic indicators. The special value CARRIER is matched against the genomic indicator name rather than the inheritance mechanism. Example: [\"AUTOSOMAL_DOMINANT\", \"CARRIER\"]", InheritanceMechanism.class)
                .required("hgvsc"),
            args -> withLinks(germlineAnnotationService.annotateMutationByHGVSc(
                args.requiredString("hgvsc"),
                args.string("referenceGenome", DEFAULT_REFERENCE_GENOME),
                args.string("tumorType"),
                args.enumList("inheritanceMechanisms", InheritanceMechanism.class)), GeneticType.GERMLINE));

        // GET /info
        add(registered, "info", "Get OncoKB information: data version, app version, OncoTree version, and levels.",
            McpSchema.object(),
            args -> cacheFetcher.getOncoKBInfo());

        // GET /levels, /levels/sensitive, /levels/resistance, /levels/prognostic, /levels/diagnostic
        add(registered, "levels", "Get all levels.", McpSchema.object(),
            args -> levelDescriptions(LevelUtils.getPublicLevels()));
        add(registered, "levels_sensitive", "Get all sensitive levels.", McpSchema.object(),
            args -> levelDescriptions(LevelUtils.getSensitiveLevels()));
        add(registered, "levels_resistance", "Get all resistance levels.", McpSchema.object(),
            args -> levelDescriptions(LevelUtils.getResistanceLevels()));
        add(registered, "levels_prognostic", "Get all prognostic levels.", McpSchema.object(),
            args -> levelDescriptions(LevelUtils.getPrognosticLevels()));
        add(registered, "levels_diagnostic", "Get all diagnostic levels.", McpSchema.object(),
            args -> levelDescriptions(LevelUtils.getDiagnosticLevels()));

        // GET /utils/cancerGeneList
        add(registered, "utils_cancerGeneList", "Get cancer gene list.",
            McpSchema.object()
                .string("version", VERSION),
            args -> body(cancerGeneService.getCancerGeneList(args.string("version"))));

        // GET /utils/allCuratedGenes
        add(registered, "utils_allCuratedGenes", "Get list of genes OncoKB curated.",
            McpSchema.object()
                .string("version", VERSION)
                .bool("includeEvidence", "Include gene summary and background", true)
                .string("hugoSymbol", "The gene symbol used in Human Genome Organisation. Gene aliases are accepted. The symbol is case-sensitive. When specified, only the curated gene matching this symbol is returned. Only supported for the latest data version, so it may be combined with version only when that version is the latest. Example: BRAF"),
            args -> body(cancerGeneService.getAllCuratedGenes(
                args.string("version"),
                args.bool("includeEvidence", true),
                args.string("hugoSymbol"))));

        tools = Collections.unmodifiableMap(registered);
    }

    Collection<McpTool> getTools() {
        return tools.values();
    }

    McpTool getTool(String name) {
        return name == null ? null : tools.get(name);
    }

    private ObjectNode withLinks(SomaticIndicatorQueryResp response, GeneticType geneticType) {
        return withLinks(response, response.getQuery(), response.getGeneExist(), geneticType);
    }

    private ObjectNode withLinks(GermlineIndicatorQueryResp response, GeneticType geneticType) {
        return withLinks(response, response.getQuery(), response.getGeneExist(), geneticType);
    }

    /**
     * Returns the REST response body with an added {@link #LINKS_FIELD} object of oncokb.org page
     * links (see {@link OncoKBPageLinks}).
     */
    private ObjectNode withLinks(Object response, Query query, Boolean geneExist, GeneticType geneticType) {
        ObjectNode body = mapper.valueToTree(response);
        ObjectNode links = body.putObject(LINKS_FIELD);
        for (Map.Entry<String, String> link : OncoKBPageLinks.forAnnotation(query, geneExist, geneticType).entrySet()) {
            links.put(link.getKey(), link.getValue());
        }
        return body;
    }

    /**
     * Same map the /levels REST endpoints return: each level mapped to its description.
     */
    static Map<LevelOfEvidence, String> levelDescriptions(Set<LevelOfEvidence> levels) {
        Map<LevelOfEvidence, String> map = new HashMap<>();
        for (LevelOfEvidence levelOfEvidence : levels) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        return map;
    }

    /**
     * Unwraps a service response, reporting a non-2xx status (for example an unknown data version)
     * as a tool error.
     */
    static <T> T body(ResponseEntity<T> response) throws McpToolException {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new McpToolException("Request failed with HTTP status " + response.getStatusCode().value() + " " + response.getStatusCode().getReasonPhrase() + ".");
        }
        return response.getBody();
    }

    private static void add(Map<String, McpTool> registered, String name, String description, McpSchema schema, McpTool.Handler handler) {
        ObjectNode inputSchema = schema.build();
        if (registered.put(name, new McpTool(name, description, inputSchema, handler)) != null) {
            throw new IllegalStateException("Duplicate MCP tool name '" + name + "'");
        }
    }
}
