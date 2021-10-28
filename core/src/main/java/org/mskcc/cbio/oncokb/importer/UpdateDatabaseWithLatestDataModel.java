/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.EvidenceBo;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.LevelUtils;
import org.mskcc.cbio.oncokb.util.TumorTypeUtils;

import java.io.IOException;
import java.util.*;

import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;

public class UpdateDatabaseWithLatestDataModel {
    public static void main(String[] args) throws IOException {
        updateAlteration();
        updateEvidence();
    }

    private static void updateAlteration() {
        for (Alteration alteration : ApplicationContextSingleton.getAlterationBo().findAll()) {
            alteration.getReferenceGenomes().add(ReferenceGenome.GRCh37);
            ApplicationContextSingleton.getAlterationBo().update(alteration);
        }
    }

    private static void updateEvidence() {
        List<Evidence> evidences = ApplicationContextSingleton.getEvidenceBo().findAll();

        for (Evidence evidence : evidences) {
            TumorType matchedTumorType = null;
            if (StringUtils.isNotEmpty(evidence.getSubtype())) {
                matchedTumorType = TumorTypeUtils.getBySubtype(evidence.getSubtype());
            }
            if (matchedTumorType == null && StringUtils.isNotEmpty(evidence.getCancerType())) {
                matchedTumorType = TumorTypeUtils.getByMainType(evidence.getCancerType());
            }
            if (matchedTumorType != null) {
                evidence.setCancerTypes(Collections.singleton(matchedTumorType));
            }
            if (evidence.getLevelOfEvidence() != null) {
                evidence.setSolidPropagationLevel(LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.SOLID));
                evidence.setLiquidPropagationLevel(LevelUtils.getDefaultPropagationLevelByTumorForm(evidence, TumorForm.LIQUID));
            }
            ApplicationContextSingleton.getEvidenceBo().update(evidence);
        }
    }
}
