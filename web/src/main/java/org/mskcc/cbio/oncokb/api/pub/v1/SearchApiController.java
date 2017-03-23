package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.apiModels.ApiListResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.EvidenceQueries;
import org.mskcc.cbio.oncokb.model.IndicatorQueryResp;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.model.Query;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.IndicatorUtils;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class SearchApiController implements SearchApi {

    public ResponseEntity<ApiObjectResp> searchGet(
        @ApiParam(value = "The query ID") @RequestParam(value = "id", required = false) String id
        , @ApiParam(value = "The gene symbol used in Human Genome Organisation.") @RequestParam(value = "hugoSymbol", required = false) String hugoSymbol
        , @ApiParam(value = "The entrez gene ID.") @RequestParam(value = "entrezGeneId", required = false) Integer entrezGeneId
        , @ApiParam(value = "Variant name.") @RequestParam(value = "variant", required = false) String variant
        , @ApiParam(value = "Consequence") @RequestParam(value = "consequence", required = false) String consequence
        , @ApiParam(value = "Protein Start") @RequestParam(value = "proteinStart", required = false) Integer proteinStart
        , @ApiParam(value = "Protein End") @RequestParam(value = "proteinEnd", required = false) Integer proteinEnd
        , @ApiParam(value = "Tumor type name. OncoTree code is supported.") @RequestParam(value = "tumorType", required = false) String tumorType
        , @ApiParam(value = "Tumor type source. OncoTree tumor types are the default setting. We may have customized version, like Quest.", defaultValue = "oncotree") @RequestParam(value = "source", required = false, defaultValue = "oncotree") String source
        , @ApiParam(value = "Level of evidences.") @RequestParam(value = "levels", required = false) String levels
        , @ApiParam(value = "Only show treatments of highest level") @RequestParam(value = "highestLevelOnly", required = false, defaultValue = "FALSE") Boolean highestLevelOnly
        , @ApiParam(value = "Query type. There maybe slight differences between different query types. Currently support web or regular.") @RequestParam(value = "queryType", required = false, defaultValue = "regular") String queryType
    ) {

        ApiObjectResp apiObjectResp = new ApiObjectResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();

        if (entrezGeneId != null && hugoSymbol != null && !GeneUtils.isSameGene(entrezGeneId, hugoSymbol)) {
            meta = MetaUtils.getBadRequestMeta("Entrez Gene ID and Hugo Symbol are not pointing to same gene.");
        } else {
            Query query = new Query();
            query.setId(id);

            if (entrezGeneId != null) {
                query.setEntrezGeneId(entrezGeneId);
            }
            query.setHugoSymbol(hugoSymbol);
            query.setAlteration(variant);
            query.setTumorType(tumorType);
            query.setConsequence(consequence);
            query.setType(queryType);
            if (proteinStart != null) {
                query.setProteinStart(proteinStart);
            }
            if (proteinEnd != null) {
                query.setProteinEnd(proteinEnd);
            }

            source = source == null ? "oncokb" : source;

            Set<LevelOfEvidence> levelOfEvidences = levels == null ? LevelUtils.getPublicAndOtherIndicationLevels() : LevelUtils.parseStringLevelOfEvidences(levels);
            apiObjectResp.setData(IndicatorUtils.processQuery(query, null, levelOfEvidences, source, highestLevelOnly));
        }
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, status);
    }

    public ResponseEntity<ApiListResp> searchPost(@ApiParam(value = "List of queries. Please see swagger.json for request body format.", required = true) @RequestBody(required = true) EvidenceQueries body) {
        ApiListResp apiListResp = new ApiListResp();
        HttpStatus status = HttpStatus.OK;
        Meta meta = MetaUtils.getOKMeta();

        List<IndicatorQueryResp> result = new ArrayList<>();

        if (body == null || body.getQueries() == null || body.getQueries().size() == 0) {
            meta = MetaUtils.getBadRequestMeta("Error on request body format");
            status = HttpStatus.BAD_REQUEST;
        } else {

            String source = body.getSource() == null ? "oncokb" : body.getSource();

            for (Query query : body.getQueries()) {
                result.add(IndicatorUtils.processQuery(query, null,
                    body.getLevels() == null ? LevelUtils.getPublicAndOtherIndicationLevels() : body.getLevels(),
                    source, body.getHighestLevelOnly()));
            }
            apiListResp.setData(result);
        }
        apiListResp.setMeta(meta);
        return new ResponseEntity<ApiListResp>(apiListResp, status);
    }
}
