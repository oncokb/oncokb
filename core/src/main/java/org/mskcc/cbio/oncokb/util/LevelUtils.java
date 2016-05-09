package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class LevelUtils {
    public static final List<LevelOfEvidence> LEVELS = Collections.unmodifiableList(
        new ArrayList<LevelOfEvidence>() {{
            add(LevelOfEvidence.LEVEL_3B);
            add(LevelOfEvidence.LEVEL_3A);
            add(LevelOfEvidence.LEVEL_2B);
            add(LevelOfEvidence.LEVEL_2A);
            add(LevelOfEvidence.LEVEL_1);
        }}
    );

    public static LevelOfEvidence getHighestLevelFromEvidence(Set<Evidence> evidences) {
        Integer highestLevelIndex = -1;

        for (Evidence evidence : evidences) {
            LevelOfEvidence level = evidence.getLevelOfEvidence();
            if (level != null) {
                Integer _index = LEVELS.indexOf(level);
                highestLevelIndex = _index > highestLevelIndex ? _index : highestLevelIndex;
            }
        }

        return highestLevelIndex > -1 ? LEVELS.get(highestLevelIndex) : null;
    }
}
