package org.mskcc.cbio.oncokb.service;

import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 11/7/17.
 */
public class JsonResultFactory {
    public static IndicatorQueryResp getIndicatorQueryResp(IndicatorQueryResp resp, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(resp)
                .onClass(IndicatorQueryResp.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*")))
                .onClass(LevelOfEvidence.class, Match.match()
                    .include("*"))
                .onClass(IndicatorQueryTreatment.class, Match.match()
                    .include("*"))
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(Query.class, Match.match()
                    .include("*")))
                .returnValue();
        } else {
            return resp;
        }
    }

    public static List<IndicatorQueryResp> getIndicatorQueryResp(List<IndicatorQueryResp> resp, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(resp)
                .onClass(IndicatorQueryResp.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*")))
                .onClass(LevelOfEvidence.class, Match.match()
                    .include("*"))
                .onClass(IndicatorQueryTreatment.class, Match.match()
                    .include("*"))
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(Query.class, Match.match()
                    .include("*")))
                .returnValue();
        } else {
            return resp;
        }
    }

    public static IndicatorQueryResp getIndicatorQueryRespWithoutGermline(IndicatorQueryResp resp) {
        if (resp != null) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(resp)
                .onClass(IndicatorQueryResp.class, Match.match()
                    .include("*")
                    .exclude("germline"))
                .onClass(Query.class, Match.match()
                    .include("*")
                    .exclude("isGermline", "inheritanceMechanism", "pathogenicity")))
                .returnValue();
        } else {
            return null;
        }
    }

    public static List<IndicatorQueryResp> getIndicatorQueryRespWithoutGermline(List<IndicatorQueryResp> resp) {
        JsonResult json = JsonResult.instance();
        return json.use(JsonView.with(resp)
            .onClass(IndicatorQueryResp.class, Match.match()
                .include("*")
                .exclude("germline"))
            .onClass(Query.class, Match.match()
                    .include("*")
                    .exclude("isGermline", "inheritanceMechanism", "pathogenicity")))
            .returnValue();
    }

    public static Gene getGene(Gene gene, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(gene)
                .onClass(Gene.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))
                ))
                .returnValue();
        } else {
            return gene;
        }
    }

    public static List<Gene> getGene(List<Gene> gene, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(gene)
                .onClass(Gene.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))
                ))
                .returnValue();
        } else {
            return gene;
        }
    }

    public static Alteration getAlteration(Alteration alteration, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(alteration)
                .onClass(Alteration.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*")))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*")))
                .returnValue();
        } else {
            return alteration;
        }
    }

    public static List<Alteration> getAlteration(List<Alteration> alterations, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(alterations)
                .onClass(Alteration.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*")))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*")))
                .returnValue();
        } else {
            return alterations;
        }
    }

    public static List<List<Alteration>> getAlteration2D(List<List<Alteration>> alterations, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(alterations)
                .onClass(Alteration.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*")))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*")))
                .returnValue();
        } else {
            return alterations;
        }
    }

    public static Evidence getEvidence(Evidence evidence, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(evidence)
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Article.class, Match.match()
                    .include("*"))
                .onClass(TumorType.class, Match.match()
                    .include("*"))
                .onClass(Treatment.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrug.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrugId.class, Match.match()
                    .include("*"))
                .onClass(LevelOfEvidence.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*"))
                .onClass(Evidence.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))))
                .returnValue();
        } else {
            return evidence;
        }
    }

    public static Set<Evidence> getEvidence(Set<Evidence> evidence, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(evidence)
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Article.class, Match.match()
                    .include("*"))
                .onClass(TumorType.class, Match.match()
                    .include("*"))
                .onClass(Treatment.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrug.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrugId.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*"))
                .onClass(Alteration.class, Match.match()
                    .include("*"))
                .onClass(Evidence.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))))
                .returnValue();
        } else {
            return evidence;
        }
    }

    public static List<Evidence> getEvidence(List<Evidence> evidence, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(evidence)
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Article.class, Match.match()
                    .include("*"))
                .onClass(TumorType.class, Match.match()
                    .include("*"))
                .onClass(Treatment.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrug.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrugId.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*"))
                .onClass(Alteration.class, Match.match()
                    .include("*"))
                .onClass(Evidence.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))))
                .returnValue();
        } else {
            return evidence;
        }
    }

    public static List<EvidenceQueryRes> getEvidenceQueryRes(List<EvidenceQueryRes> resp, String fields) {
        if (fields != null && !fields.isEmpty()) {
            JsonResult json = JsonResult.instance();
            return json.use(JsonView.with(resp)
                .onClass(Drug.class, Match.match()
                    .include("*"))
                .onClass(ArticleAbstract.class, Match.match()
                    .include("*"))
                .onClass(VariantConsequence.class, Match.match()
                    .include("*"))
                .onClass(Article.class, Match.match()
                    .include("*"))
                .onClass(TumorType.class, Match.match()
                    .include("*"))
                .onClass(Treatment.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrug.class, Match.match()
                    .include("*"))
                .onClass(TreatmentDrugId.class, Match.match()
                    .include("*"))
                .onClass(LevelOfEvidence.class, Match.match()
                    .include("*"))
                .onClass(Gene.class, Match.match()
                    .include("*"))
                .onClass(Alteration.class, Match.match()
                    .include("*"))
                .onClass(EvidenceQueryRes.class, Match.match()
                    .exclude("*")
                    .include(fields.split("\\s*,\\s*"))))
                .returnValue();
        } else {
            return resp;
        }
    }
}

