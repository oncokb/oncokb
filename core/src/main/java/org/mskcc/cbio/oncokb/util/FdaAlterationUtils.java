package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class FdaAlterationUtils {
    public static LevelOfEvidence convertToFdaLevel(LevelOfEvidence level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case LEVEL_1:
            case LEVEL_R1:
            case LEVEL_2:
                return LevelOfEvidence.LEVEL_Fda2;
            case LEVEL_3A:
            case LEVEL_3B:
            case LEVEL_4:
            case LEVEL_R2:
                return LevelOfEvidence.LEVEL_Fda3;
            default:
                return null;
        }
    }
}
