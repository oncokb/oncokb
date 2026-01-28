package org.mskcc.cbio.oncokb.api.pvt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceQueryRes;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.Tag;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.EvidenceTypeUtils;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TagsController {
    @RequestMapping(value = "/tags/{entrezGeneId}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<Tag>> getTagsByEntrezGeneId(@PathVariable int entrezGeneId) {
        return ResponseEntity.ok().body(ApplicationContextSingleton.getTagBo().findTagsByEntrezGeneId(entrezGeneId));
    }

    @RequestMapping(value = "/tags/{hugoSymbol}/{name}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Tag> getTagByHugoSymbolAndName(@PathVariable String hugoSymbol, @PathVariable String name) throws Exception {
        return ResponseEntity.ok().body(ApplicationContextSingleton.getTagBo().findTagByHugoSymbolAndName(hugoSymbol, name));
    }

    @RequestMapping(value = "/tags/{hugoSymbol}/{name}/{tumorType}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Tag> getTagByHugoSymbolAndNameAndTumorType(@PathVariable String hugoSymbol, @PathVariable String name, @PathVariable String tumorType) throws Exception {
        Tag tag = ApplicationContextSingleton.getTagBo().findTagByHugoSymbolAndName(hugoSymbol, name);
        TumorType matchedTumorType = ApplicationContextSingleton.getTumorTypeBo().getByName(tumorType);
        List<TumorType> relevantTumorTypes = TumorTypeUtils.findRelevantTumorTypes(tumorType);

        EvidenceQueryRes evidenceQuery = new EvidenceQueryRes();
        evidenceQuery.setQuery(new Query());
        evidenceQuery.getQuery().setTumorType(tumorType);
        evidenceQuery.setGene(tag.getGene());
        evidenceQuery.setExactMatchedTumorType(matchedTumorType);
        evidenceQuery.setOncoTreeTypes(relevantTumorTypes);

        Set<Evidence> treatmentEvidences = new HashSet<>();
        Set<Evidence> diagnosticEvidences = new HashSet<>();
        Set<Evidence> prognosticEvidences = new HashSet<>();
        for (Evidence evidence : tag.getEvidences()) {
            if (EvidenceTypeUtils.getTreatmentEvidenceTypes().contains(evidence.getEvidenceType())) {
                treatmentEvidences.add(evidence);
            } else if (LevelUtils.isPrognosticLevel(evidence.getLevelOfEvidence())) {
                prognosticEvidences.add(evidence);
            } else if (LevelUtils.isDiagnosticLevel(evidence.getLevelOfEvidence())) {
                diagnosticEvidences.add(evidence);
            }
        }

        Set<Evidence> evidences = EvidenceUtils.filterEvidence(tag.getEvidences(), evidenceQuery, false);

        tag.setEvidences(new HashSet<>(evidences));
        return ResponseEntity.ok().body(tag);
    }
}