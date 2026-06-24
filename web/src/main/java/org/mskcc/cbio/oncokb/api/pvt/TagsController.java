package org.mskcc.cbio.oncokb.api.pvt;

import java.util.List;
import java.util.Set;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceQueryRes;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Oncogenicity;
import org.mskcc.cbio.oncokb.model.OncogenicityEntity;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.model.SpecialTumorType;
import org.mskcc.cbio.oncokb.model.Tag;
import org.mskcc.cbio.oncokb.model.TumorType;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.EvidenceUtils;
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
        List<Tag> tags = ApplicationContextSingleton.getTagBo().findTagsByEntrezGeneId(entrezGeneId);
        for (Tag tag : tags) {
            EvidenceQueryRes evidenceQuery = new EvidenceQueryRes();
            evidenceQuery.setQuery(new Query());
            evidenceQuery.setGene(tag.getGene());
            addOncogenicMutationsEvidenceIfApplicable(tag, evidenceQuery);
        }
        return ResponseEntity.ok().body(tags);
    }

    @RequestMapping(value = "/tags/{hugoSymbol}/{name}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<Tag> getTagByHugoSymbolAndName(@PathVariable String hugoSymbol, @PathVariable String name) throws Exception {
        Tag tag = ApplicationContextSingleton.getTagBo().findTagByHugoSymbolAndName(hugoSymbol, name);
        EvidenceQueryRes evidenceQuery = new EvidenceQueryRes();
        evidenceQuery.setQuery(new Query());
        evidenceQuery.setGene(tag.getGene());
        addOncogenicMutationsEvidenceIfApplicable(tag, evidenceQuery);

        return ResponseEntity.ok().body(tag);
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
        addOncogenicMutationsEvidenceIfApplicable(tag, evidenceQuery);

        return ResponseEntity.ok().body(tag);
    }

    private static void addOncogenicMutationsEvidenceIfApplicable(Tag tag, EvidenceQueryRes evidenceQuery) {
        boolean addOncogenicMutations = false;
        for (OncogenicityEntity oncogenicityEntity : tag.getOncogenicities()) {
            if (oncogenicityEntity.getOncogenicity().equals(Oncogenicity.YES) || oncogenicityEntity.getOncogenicity().equals(Oncogenicity.LIKELY)) {
                addOncogenicMutations = true;
                break;
            }
        }        
        if (addOncogenicMutations) {
            List<Alteration> oncogenicMutations = AlterationUtils.findOncogenicMutations(
                AlterationUtils.getAllAlterations(null, tag.getGene())
            );
            evidenceQuery.setAlterations(oncogenicMutations);
            tag.getEvidences().addAll(EvidenceUtils.getAlterationEvidences(oncogenicMutations));
        }
        Set<Evidence> evidences = EvidenceUtils.filterEvidence(tag.getEvidences(), evidenceQuery, false);
        if (evidences.stream().noneMatch(evi -> evi.getEvidenceType().equals(EvidenceType.TUMOR_TYPE_SUMMARY))) {
            for (Evidence evi : tag.getEvidences()) {
                if (evi.getEvidenceType().equals(EvidenceType.TUMOR_TYPE_SUMMARY) && 
                evi.getCancerTypes().contains(ApplicationContextSingleton.getTumorTypeBo().getBySpecialTumor(SpecialTumorType.OTHER_TUMOR_TYPES))) {
                    evi.setDescription(evi.getDescription().replace("[[variant]]", tag.getName()));
                    evidences.add(evi);
                }
            }
        }
        tag.setEvidences(evidences);
    }
}