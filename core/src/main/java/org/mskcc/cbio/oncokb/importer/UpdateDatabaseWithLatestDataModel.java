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
        updateLegacyCancerType();
        updateEvidence();
    }

    private static void updateAlteration() {
        for (Alteration alteration : ApplicationContextSingleton.getAlterationBo().findAll()) {
            alteration.getReferenceGenomes().add(ReferenceGenome.GRCh37);
            ApplicationContextSingleton.getAlterationBo().update(alteration);
        }
    }

    private static void updateLegacyCancerType() {
        for (Evidence evidence : ApplicationContextSingleton.getEvidenceBo().findAll()) {
            if (StringUtils.isNotEmpty(evidence.getSubtype()) && evidence.getSubtype().equals("DLBCL")) {
                evidence.setSubtype("DLBCLNOS");
            }
            if (StringUtils.isNotEmpty(evidence.getSubtype()) && evidence.getSubtype().equals("ALL")) {
                evidence.setSubtype("BLL");
            }
            if (StringUtils.isNotEmpty(evidence.getCancerType()) && evidence.getCancerType().equals("Histiocytic Disorder")) {
                evidence.setCancerType("Histiocytosis");
            }
            if (StringUtils.isNotEmpty(evidence.getCancerType()) && evidence.getCancerType().equals("Myelodysplasia")) {
                evidence.setSubtype("MDS/MPN");
                evidence.setCancerType("Myelodysplastic/Myeloproliferative Neoplasms");
            }
            if (StringUtils.isNotEmpty(evidence.getCancerType()) && evidence.getCancerType().equals("Myeloproliferative Neoplasm")) {
                evidence.setSubtype("MDS/MPN");
                evidence.setCancerType("Myelodysplastic/Myeloproliferative Neoplasms");
            }
            ApplicationContextSingleton.getEvidenceBo().update(evidence);
        }
    }

    private static void updateEvidence() {
        List<Evidence> evidences = ApplicationContextSingleton.getEvidenceBo().findAll();

        for (Evidence evidence : evidences) {
            TumorType matchedTumorType = null;
            if (StringUtils.isNotEmpty(evidence.getSubtype())) {
                matchedTumorType = TumorTypeUtils.getByCode(evidence.getSubtype());
                if (matchedTumorType == null) {
                    System.out.println("Cannot find the cancer type for subtype " + evidence.getSubtype());
                }
            } else if (StringUtils.isNotEmpty(evidence.getCancerType())) {
                matchedTumorType = TumorTypeUtils.getByMainType(evidence.getCancerType());
                if (matchedTumorType == null) {
                    System.out.println("Cannot find the cancer type for main type " + evidence.getCancerType());
                }
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
