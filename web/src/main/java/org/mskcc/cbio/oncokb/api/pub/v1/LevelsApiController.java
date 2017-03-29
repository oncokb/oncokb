package org.mskcc.cbio.oncokb.api.pub.v1;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.ApiObjectResp;
import org.mskcc.cbio.oncokb.apiModels.Meta;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.MetaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class LevelsApiController implements LevelsApi {

    public ResponseEntity<ApiObjectResp> levelsGet() {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        Meta meta = MetaUtils.getOKMeta();

        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getAllLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();
        
        for(LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        apiObjectResp.setData(map);
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiObjectResp> levelsResistenceGet() {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        Meta meta = MetaUtils.getOKMeta();

        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getResistanceLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();

        for(LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        apiObjectResp.setData(map);
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.OK);
    }

    public ResponseEntity<ApiObjectResp> levelsSensitiveGet() {
        ApiObjectResp apiObjectResp = new ApiObjectResp();
        Meta meta = MetaUtils.getOKMeta();
        
        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getSensitiveLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();

        for(LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        apiObjectResp.setData(map);
        apiObjectResp.setMeta(meta);
        return new ResponseEntity<ApiObjectResp>(apiObjectResp, HttpStatus.OK);
    }
}
