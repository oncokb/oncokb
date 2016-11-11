package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class GenesApiController implements GenesApi {

    public ResponseEntity<ApiListResp> genesEntrezGeneIdEvidencesGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    ) {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();

        if (entrezGeneId == null) {
            meta = MetaUtils.getBadRequestMeta("Please specific entrezGeneId.");
            apiListResp.setMeta(meta);
            return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.BAD_REQUEST);
        }

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            meta = MetaUtils.getBadRequestMeta("Gene is not available.");
            apiListResp.setMeta(meta);
            return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.BAD_REQUEST);
        }

        Set<EvidenceType> evidenceTypeSet = new HashSet<>();
        if (evidenceTypes != null) {
            for (String type : evidenceTypes.trim().split("\\s*,\\s*")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypeSet.add(et);
            }
        } else {
            evidenceTypeSet.add(EvidenceType.GENE_SUMMARY);
            evidenceTypeSet.add(EvidenceType.GENE_BACKGROUND);
        }

        Map<Gene, Set<Evidence>> map = EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypeSet);
        Set<Evidence> evidences = map.get(gene);

        if (evidences == null) {
            evidences = new HashSet<Evidence>();
        }

        Set<GeneEvidence> geneEvidences = new HashSet<>();

        for (Evidence evidence : evidences) {
            geneEvidences.add(new GeneEvidence(evidence));
        }

        apiListResp.setMeta(meta);
        apiListResp.setData(new ArrayList(geneEvidences));
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiObjectResp> genesEntrezGeneIdGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
    ) {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        Meta meta = MetaUtils.getOKMeta();

        if (entrezGeneId == null) {
            meta = MetaUtils.getBadRequestMeta("Please specific entrezGeneId.");
            apiObjectResp.setMeta(meta);
            return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.BAD_REQUEST);
        }

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            meta = MetaUtils.getBadRequestMeta("Gene is not available.");
            apiObjectResp.setMeta(meta);
            return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.BAD_REQUEST);
        }

        apiObjectResp.setData(gene);
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> genesEntrezGeneIdVariantsGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
    ) {
        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();

        if (entrezGeneId == null) {
            meta = MetaUtils.getBadRequestMeta("Please specific entrezGeneId.");
            apiListResp.setMeta(meta);
            return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.BAD_REQUEST);
        }

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            meta = MetaUtils.getBadRequestMeta("Gene is not available.");
            apiListResp.setMeta(meta);
            return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.BAD_REQUEST);
        }

        Set<Alteration> alterations = AlterationUtils.getAllAlterations(gene);

        if (alterations == null) {
            alterations = new HashSet<>();
        }
        apiListResp.setData(new ArrayList(alterations));
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> genesGet() {
        Set<Gene> genes = GeneUtils.getAllGenes();
        if (genes == null) {
            genes = new HashSet<>();
        }

        ApiListResp apiListResp = new ApiListResp();
        Meta meta = MetaUtils.getOKMeta();
        apiListResp.setData(new ArrayList(genes));
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiListResp> genesLookupGet(@ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
    ) {
        Meta meta = MetaUtils.getOKMeta();
        ApiListResp apiListResp = new ApiListResp();
        Set<Gene> genes = new HashSet<>();
        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            meta = MetaUtils.getBadRequestMeta("Entrez Gene ID and Hugo Symbol are not pointing to same gene.");
        } else {
            if (hugoSymbol != null) {
                Set<Gene> blurGenes = GeneUtils.searchGene(hugoSymbol);
                if (blurGenes != null) {
                    genes.addAll(blurGenes);
                }
            }

            if (entrezGeneId != null) {
                Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
                if (gene != null) {
                    if (!genes.contains(gene)) {
                        genes = new HashSet<>();
                    }
                }
            }
        }

        apiListResp.setMeta(meta);
        apiListResp.setData(new ArrayList(genes));
        return new ResponseEntity<ApiListResp>(apiListResp, HttpStatus.OK);
    }

}
