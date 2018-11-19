package org.mskcc.cbio.oncokb.api.pub.v1;

import org.apache.commons.collections.map.HashedMap;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Set;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-19T19:28:21.941Z")

@Controller
public class LevelsApiController implements LevelsApi {

    public ResponseEntity<Map<LevelOfEvidence, String>> levelsGet() {
        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getAllLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();

        for (LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public ResponseEntity<Map<LevelOfEvidence, String>> levelsResistanceGet() {
        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getResistanceLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();

        for (LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    public ResponseEntity<Map<LevelOfEvidence, String>> levelsSensitiveGet() {
        Set<LevelOfEvidence> levelOfEvidenceSet = LevelUtils.getSensitiveLevels();
        Map<LevelOfEvidence, String> map = new HashedMap();

        for (LevelOfEvidence levelOfEvidence : levelOfEvidenceSet) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
