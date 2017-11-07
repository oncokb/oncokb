package org.mskcc.cbio.oncokb.service;

import com.monitorjbl.json.JsonResult;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.Match;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

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
}
