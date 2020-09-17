package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.service.JsonResultFactory;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class GenesApiController implements GenesApi {

    public ResponseEntity<List<GeneEvidence>> genesEntrezGeneIdEvidencesGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
        , @ApiParam(value = "Separate by comma. Evidence type includes GENE_SUMMARY, GENE_BACKGROUND") @RequestParam(value = "evidenceTypes", required = false) String evidenceTypes
    ) {

        List<GeneEvidence> evidenceList = new ArrayList<>();
        if (entrezGeneId == null) {
            return new ResponseEntity<>(evidenceList, HttpStatus.BAD_REQUEST);
        }

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            return new ResponseEntity<>(evidenceList, HttpStatus.BAD_REQUEST);
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

        evidenceList.addAll(geneEvidences);
        return new ResponseEntity<>(evidenceList, HttpStatus.OK);
    }

    public ResponseEntity<Gene> genesEntrezGeneIdGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) {
        Gene gene = null;
        if (entrezGeneId == null) {
            return new ResponseEntity<>(gene, HttpStatus.BAD_REQUEST);
        }

        gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            return new ResponseEntity<>(gene, HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().body(JsonResultFactory.getGene(gene, fields));
    }

    public ResponseEntity<List<Alteration>> genesEntrezGeneIdVariantsGet(
        @ApiParam(value = "The entrez gene ID.", required = true) @PathVariable("entrezGeneId") Integer entrezGeneId
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) {
        List<Alteration> alterationList = new ArrayList<>();
        if (entrezGeneId == null) {
            return new ResponseEntity<>(alterationList, HttpStatus.BAD_REQUEST);
        }

        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);

        if (gene == null) {
            return new ResponseEntity<>(alterationList, HttpStatus.BAD_REQUEST);
        }

        Set<Alteration> alterations = AlterationUtils.getAllAlterations(null, gene);

        if (alterations == null) {
            alterations = new HashSet<>();
        }
        alterationList.addAll(alterations);
        return ResponseEntity.ok().body(JsonResultFactory.getAlteration(alterationList, fields));
    }

    public ResponseEntity<List<Gene>> genesGet(
        @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) {
        Set<Gene> genes = GeneUtils.getAllGenes();
        if (genes == null) {
            genes = new HashSet<>();
        }
        List<Gene> geneList = new ArrayList<>(genes);
        return ResponseEntity.ok(JsonResultFactory.getGene(geneList, fields));
    }

    public ResponseEntity genesLookupGet(
        @ApiParam(value = "The gene symbol used in Human Genome Organisation. (Deprecated, use query instead)") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID. (Deprecated, use query instead)") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "The search query, it could be hugoSymbol or entrezGeneId.") @RequestParam(value = "query", required = false) String query
        , @ApiParam(value = "The fields to be returned.") @RequestParam(value = "fields", required = false) String fields
    ) {

        LinkedHashSet<Gene> genes = new LinkedHashSet<>();
        if (query != null) {
            genes.addAll(GeneUtils.searchGene(query, true));
            genes.addAll(GeneUtils.searchGene(query, false));
        } else if (entrezGeneId != null) {
            genes.add(GeneUtils.getGeneByEntrezId(entrezGeneId));
        } else if (hugoSymbol != null) {
            genes.add(GeneUtils.getGeneByHugoSymbol(hugoSymbol));
        }
        return ResponseEntity.ok().body(JsonResultFactory.getGene(new ArrayList<>(genes), fields));
    }

}
