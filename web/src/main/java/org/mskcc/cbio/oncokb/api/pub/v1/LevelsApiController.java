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
        Set<LevelOfEvidence> publicLevels = LevelUtils.getPublicLevels();
        return new ResponseEntity<>(getMap(publicLevels), HttpStatus.OK);
    }

    public ResponseEntity<Map<LevelOfEvidence, String>> levelsResistanceGet() {
        return new ResponseEntity<>(getMap(LevelUtils.getResistanceLevels()), HttpStatus.OK);
    }

    public ResponseEntity<Map<LevelOfEvidence, String>> levelsSensitiveGet() {
        return new ResponseEntity<>(getMap(LevelUtils.getSensitiveLevels()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<LevelOfEvidence, String>> levelsPrognosticGet() {
        return new ResponseEntity<>(getMap(LevelUtils.getPrognosticLevels()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<LevelOfEvidence, String>> levelsDiagnosticGet() {
        return new ResponseEntity<>(getMap(LevelUtils.getDiagnosticLevels()), HttpStatus.OK);
    }

    private Map<LevelOfEvidence, String> getMap(Set<LevelOfEvidence> levels) {
        Map<LevelOfEvidence, String> map = new HashedMap();

        for (LevelOfEvidence levelOfEvidence : levels) {
            map.put(levelOfEvidence, levelOfEvidence.getDescription());
        }
        return map;
    }
}
