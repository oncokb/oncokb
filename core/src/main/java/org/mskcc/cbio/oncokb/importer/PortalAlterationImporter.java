/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @author jiaojiao Sep/8/2017 Import alteration data from portal database
 */
public class PortalAlterationImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    public static void main(String[] args) throws IOException {
        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
        String sampleListsUrl = "http://cbioportal.mskcc.org/api/studies/msk_impact_2017/sample-lists?projection=SUMMARY";
        String sampleListsResult = FileUtils.readMSKPortal(sampleListsUrl);
        JSONArray sampleListsJSONResult = new JSONArray(sampleListsResult);
        for (int i = 0; i < sampleListsJSONResult.length(); i++) {
            JSONObject jObject = sampleListsJSONResult.getJSONObject(i);
            String category = jObject.getString("category");
            String sampleListId = jObject.getString("sampleListId");
            if (category != null && category.equalsIgnoreCase("other") && sampleListId != null && !sampleListId.equalsIgnoreCase("msk_impact_2017_NA")) {
                String cancerType = jObject.getString("name");
                String mutationUrl = "https://cbioportal.mskcc.org/api/molecular-profiles/msk_impact_2017_mutations/mutations?sampleListId=" + sampleListId + "&projection=DETAILED";
                String mutationResult = FileUtils.readMSKPortal(mutationUrl);
                JSONArray mutationJSONResult = new JSONArray(mutationResult);
                LOGGER.info("*****************************************************************************");
                LOGGER.info("Importing for {}", cancerType);
                for (int j = 0; j < mutationJSONResult.length(); j++) {
                    JSONObject sampleObject = mutationJSONResult.getJSONObject(j);
                    String sampleId = sampleObject.getString("sampleId");
                    Integer entrezGeneId = sampleObject.getInt("entrezGeneId");
                    Gene matchedGene = GeneUtils.getGeneByEntrezId(entrezGeneId);
                    if (matchedGene != null) {
                        String proteinChange = sampleObject.getString("proteinChange");
                        Integer proteinStartPosition = sampleObject.getInt("proteinPosStart");
                        Integer proteinEndPosition = sampleObject.getInt("proteinPosEnd");
                        String mutationType = sampleObject.getString("mutationType");
                        PortalAlteration portalAlteration = new PortalAlteration(cancerType.substring(12), sampleListId, sampleId, matchedGene, proteinChange, proteinStartPosition, proteinEndPosition, mutationType);
                        portalAlterationBo.save(portalAlteration);
                    }
                }
                DecimalFormat myFormatter = new DecimalFormat("##.##");
                String output = myFormatter.format(100 * (i + 1) / sampleListsJSONResult.length());
                LOGGER.info("Importing {}% done.", output);
            }
        }
        LOGGER.info("Finished.");
    }
}
