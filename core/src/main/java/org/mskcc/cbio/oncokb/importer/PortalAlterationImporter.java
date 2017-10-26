/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author jiaojiao Sep/8/2017 Import alteration data from portal database
 */
public class PortalAlterationImporter {

    private static List<Alteration> findAlterationList(Gene gene, String proteinChange, String mutation_type, Integer proteinStartPosition, Integer proteinEndPosition) {
        List<Alteration> alterations = new ArrayList<>();
        List<Alteration> alterationsSet = new ArrayList<>();
        HashMap<String, String[]> mapper = new HashMap<>();
        mapper.put("Targeted_Region", new String[]{"inframe_deletion", "inframe_insertion"});
        mapper.put("COMPLEX_INDEL", new String[]{"inframe_deletion", "inframe_insertion"});
        mapper.put("Indel", new String[]{"frameshift_variant", "inframe_deletion", "inframe_insertion"});
        mapper.put("In_Frame_Del", new String[]{"inframe_deletion", "feature_truncation"});
        mapper.put("3'Flank", new String[]{"any"});
        mapper.put("5'Flank", new String[]{"any"});
        mapper.put("ESSENTIAL_SPLICE_SITE", new String[]{"feature_truncation"});
        mapper.put("Exon skipping", new String[]{"inframe_deletion"});
        mapper.put("Frameshift deletion", new String[]{"frameshift_variant"});
        mapper.put("Frameshift insertion", new String[]{"frameshift_variant"});
        mapper.put("FRAMESHIFT_CODING", new String[]{"frameshift_variant"});
        mapper.put("Frame_Shift_Del", new String[]{"frameshift_variant"});
        mapper.put("Frame_Shift_Ins", new String[]{"frameshift_variant"});
        mapper.put("Fusion", new String[]{"fusion"});
        mapper.put("In_Frame_Ins", new String[]{"inframe_insertion"});
        mapper.put("Missense", new String[]{"missense_variant"});
        mapper.put("Missense_Mutation", new String[]{"missense_variant"});
        mapper.put("Nonsense_Mutation", new String[]{"stop_gained"});
        mapper.put("Nonstop_Mutation", new String[]{"stop_lost"});
        mapper.put("Splice_Site", new String[]{"splice_region_variant"});
        mapper.put("Splice_Site_Del", new String[]{"splice_region_variant"});
        mapper.put("Splice_Site_SNP", new String[]{"splice_region_variant"});
        mapper.put("splicing", new String[]{"splice_region_variant"});
        mapper.put("Splice_Region", new String[]{"splice_region_variant"});
        mapper.put("Translation_Start_Site", new String[]{"start_lost"});
        mapper.put("vIII deletion", new String[]{"any"});
        mapper.put("exon14skip", new String[]{"inframe_deletion"});
        mapper.put("frameshift", new String[]{"frameshift_variant"});
        mapper.put("nonframeshift insertion", new String[]{"frameshift_variant", "inframe_deletion", "inframe_insertion"});
        mapper.put("nonframeshift_deletion", new String[]{"frameshift_variant", "inframe_deletion", "inframe_insertion"});
        mapper.put("nonframeshift_insertion", new String[]{"frameshift_variant", "inframe_deletion", "inframe_insertion"});
        mapper.put("Nonsense", new String[]{"stop_gained"});
        mapper.put("splice", new String[]{"splice_region_variant"});
        mapper.put("Splice_Site_Indel", new String[]{"splice_region_variant"});

        String[] consequences = mapper.get(mutation_type);
        if (consequences == null) {
            System.out.println("No mutation type mapping for " + mutation_type);
        } else {
            for (String consequence : consequences) {
                Alteration alt = AlterationUtils.getAlteration(gene == null ? null : gene.getHugoSymbol(),
                        proteinChange, null, consequence, proteinStartPosition, proteinEndPosition);
                AlterationUtils.annotateAlteration(alt, alt.getAlteration());
                alterations.addAll(AlterationUtils.getRelevantAlterations(alt));
            }
            alterationsSet = AlterationUtils.excludeVUS(alterations);
        }

        return alterationsSet;
    }

    public static void main(String[] args) throws IOException {
//        String geneUrl = "http://oncokb.org/legacy-api/gene.json";
//        String geneResult = FileUtils.readRemote(geneUrl);
//        JSONArray geneJSONResult = new JSONArray(geneResult);
//        Set entrezGeneIds = new HashSet<>();
//        for (int i = 0; i < geneJSONResult.length(); i++) {
//            JSONObject jObject = geneJSONResult.getJSONObject(i);
//            entrezGeneIds.add(jObject.getInt("entrezGeneId"));
//        }
//        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
//        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
//        String sampleListsUrl = "http://www.cbioportal.org/api/studies/msk_impact_2017/sample-lists?projection=SUMMARY";
//        String sampleListsResult = FileUtils.readRemote(sampleListsUrl);
//        JSONArray sampleListsJSONResult = new JSONArray(sampleListsResult);
//        for (int i = 0;i < sampleListsJSONResult.length(); i++) {
//            JSONObject jObject = sampleListsJSONResult.getJSONObject(i);
//            String category = jObject.getString("category");
//            String sampleListId = jObject.getString("sampleListId");
//            if (category != null && category.equalsIgnoreCase("other") && sampleListId != null && !sampleListId.equalsIgnoreCase("msk_impact_2017_NA")) {
//                String cancerType = jObject.getString("name");
//                String mutationUrl = "http://www.cbioportal.org/api/molecular-profiles/msk_impact_2017_mutations/mutations?sampleListId=" + sampleListId + "&projection=DETAILED";
//                String muttionResult = FileUtils.readRemote(mutationUrl);
//                JSONArray mutationJSONResult = new JSONArray(muttionResult);
//                System.out.println("*****************************************************************************");
//                System.out.println("Importing for " + cancerType);
//                for (int j = 0; j < mutationJSONResult.length(); j++) {
//                    JSONObject sampleObject = mutationJSONResult.getJSONObject(j);
//                    String sampleId = sampleObject.getString("sampleId");
//                    Integer entrezGeneId = sampleObject.getInt("entrezGeneId");
//                    if (entrezGeneIds.contains(entrezGeneId)) {
//                        Gene gene = GeneUtils.getGeneByEntrezId(entrezGeneId);
//                        String proteinChange = sampleObject.getString("proteinChange");
//                        Integer proteinStartPosition = sampleObject.getInt("startPosition");
//                        Integer proteinEndPosition = sampleObject.getInt("endPosition");
//                        String mutationType = sampleObject.getString("mutationType");
//                        PortalAlteration portalAlteration = new PortalAlteration(cancerType.substring(12), sampleListId, sampleId, gene, proteinChange, proteinStartPosition, proteinEndPosition, mutationType);
//                        portalAlterationBo.save(portalAlteration);
//                        List<Alteration> oncoKBAlterations = findAlterationList(gene, proteinChange, mutationType, proteinStartPosition, proteinEndPosition);
//                        for (Alteration oncoKBAlteration : oncoKBAlterations) {
//                            Set<PortalAlteration> portalAlterations = oncoKBAlteration.getPortalAlterations();
//                            portalAlterations.add(portalAlteration);
//
//                            oncoKBAlteration.setPortalAlterations(portalAlterations);
//                            alterationBo.update(oncoKBAlteration);
//                        }
//                    }
//                }
//                DecimalFormat myFormatter = new DecimalFormat("##.##");
//                String output = myFormatter.format(100 * (i + 1) / sampleListsJSONResult.length());
//                System.out.println("Importing " + output + "% done.");
//            }
//        }

        List<PortalAlteration> portalAlterations = ApplicationContextSingleton.getPortalAlterationBo().findAll();
        for(PortalAlteration portalAlteration : portalAlterations) {
            List<Alteration> oncoKBAlterations = findAlterationList(portalAlteration.getGene(), portalAlteration.getProteinChange(), portalAlteration.getAlterationType(), portalAlteration.getProteinStartPosition(), portalAlteration.getProteinEndPosition());
            for (Alteration oncoKBAlteration : oncoKBAlterations) {
                Set<PortalAlteration> PA = oncoKBAlteration.getPortalAlterations();
                PA.add(portalAlteration);

                oncoKBAlteration.setPortalAlterations(PA);
                ApplicationContextSingleton.getAlterationBo().update(oncoKBAlteration);
            }
        }
        System.out.println("Finished.");
    }
}
