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
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author jiaojiao April/14/2016 Import alteration data from portal database
 */
public class PortalAlterationImporter {

    public static Set<Alteration> findAlterationList(Gene gene, String proteinChange, String mutation_type, Integer proteinStartPosition, Integer proteinEndPosition) {
        List<Alteration> alterations = new ArrayList<>();
        Set<Alteration> alterationsSet = new HashSet<>();
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
                alterations.addAll(AlterationUtils.getRelevantAlterations(gene, proteinChange, consequence, proteinStartPosition, proteinEndPosition));
            }
            alterationsSet = AlterationUtils.excludeVUS(new HashSet<>(alterations));
        }

        return alterationsSet;
    }

    public static void main(String[] args) throws IOException {

        JSONObject jObject = null;
        PortalAlteration portalAlteration = null;
        String geneUrl = "http://oncokb.org/legacy-api/gene.json";
        String geneResult = FileUtils.readRemote(geneUrl);
        JSONArray geneJSONResult = new JSONArray(geneResult);
        String genes[] = new String[geneJSONResult.length()];
        for (int i = 0; i < geneJSONResult.length(); i++) {
            jObject = geneJSONResult.getJSONObject(i);
            genes[i] = jObject.get("hugoSymbol").toString();
        }
        String joinedGenes = StringUtils.join(genes, ",");
        String studies[] = {"blca_tcga_pub", "brca_tcga_pub2015", "cesc_tcga", "coadread_tcga_pub", "hnsc_tcga_pub", "kich_tcga_pub", "kirc_tcga_pub", "kirp_tcga", "lihc_tcga", "luad_tcga_pub", "lusc_tcga_pub", "lgggbm_tcga_pub", "ov_tcga_pub", "thca_tcga_pub", "prad_tcga_pub", "sarc_tcga", "skcm_tcga", "stad_tcga_pub", "tgct_tcga", "ucec_tcga_pub"};
        String joinedStudies = StringUtils.join(studies, ",");
        String studyUrl = "http://www.cbioportal.org/api/studies?study_ids=" + joinedStudies;
        String studyResult = FileUtils.readRemote(studyUrl);
        JSONArray studyJSONResult = new JSONArray(studyResult);
        PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
        for (int j = 0; j < studyJSONResult.length(); j++) {
            jObject = studyJSONResult.getJSONObject(j);
            if (jObject.has("id") && jObject.has("type_of_cancer")) {
                String cancerStudy = jObject.get("id").toString();
                String cancerType = jObject.get("type_of_cancer").toString();
                System.out.println("*****************************************************************************");
                System.out.println("Importing portal alteration data for " + jObject.get("name").toString());
                //get sequenced sample list for one study
                String sequencedSamplesUrl = "http://www.cbioportal.org/api/samplelists?sample_list_ids=" + cancerStudy + "_sequenced";
                String sequencedSamplesResult = FileUtils.readRemote(sequencedSamplesUrl);
                JSONArray sequencedSamplesJSONResult = new JSONArray(sequencedSamplesResult);
                if (sequencedSamplesJSONResult != null && sequencedSamplesJSONResult.length() > 0) {
                    JSONArray sequencedSamples = (JSONArray) sequencedSamplesJSONResult.getJSONObject(0).get("sample_ids");

                    String genetic_profile_id = cancerStudy + "_mutations";
                    String sample_list_id = cancerStudy + "_sequenced";
                    String profileDataUrl = "http://www.cbioportal.org/api/geneticprofiledata?genetic_profile_ids=" + genetic_profile_id + "&genes=" + joinedGenes + "&sample_list_id=" + sample_list_id;
                    String alterationResult = FileUtils.readRemote(profileDataUrl);
                    JSONArray alterationJSONResult = new JSONArray(alterationResult);

                    for (int m = 0; m < alterationJSONResult.length(); m++) {
                        jObject = alterationJSONResult.getJSONObject(m);
                        String proteinChange = jObject.getString("amino_acid_change");
                        Integer proteinStartPosition = jObject.getInt("protein_start_position");
                        Integer proteinEndPosition = jObject.getInt("protein_end_position");
                        String mutation_type = jObject.getString("mutation_type");
                        String hugo_gene_symbol = jObject.getString("hugo_gene_symbol");
                        Integer entrez_gene_id = jObject.getInt("entrez_gene_id");
                        String sampleId = jObject.getString("sample_id");


                        Gene gene = GeneUtils.getGene(entrez_gene_id, hugo_gene_symbol);

                        portalAlteration = new PortalAlteration(cancerType, cancerStudy, sampleId, gene, proteinChange, proteinStartPosition, proteinEndPosition, mutation_type);
                        portalAlterationBo.save(portalAlteration);

                        Set<PortalAlteration> portalAlterations = new HashSet<>();

                        Set<Alteration> oncoKBAlterations = findAlterationList(gene, proteinChange, mutation_type, proteinStartPosition, proteinEndPosition);

                        for (Alteration oncoKBAlteration : oncoKBAlterations) {
                            portalAlterations = oncoKBAlteration.getPortalAlterations();
                            portalAlterations.add(portalAlteration);

                            AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
                            oncoKBAlteration.setPortalAlterations(portalAlterations);
                            alterationBo.update(oncoKBAlteration);
                        }

                        //remove saved sample from sequenced sample list 
                        for (int n = 0; n < sequencedSamples.length(); n++) {
                            if (sequencedSamples.get(n).equals(sampleId)) {
                                sequencedSamples.remove(n);
                                break;
                            }
                        }
                    }
                    //save samples that don't have mutations
                    if (sequencedSamples.length() > 0) {
                        for (int p = 0; p < sequencedSamples.length(); p++) {
                            portalAlteration = new PortalAlteration(cancerType, cancerStudy, sequencedSamples.getString(p), null, null, null, null, null);
                            portalAlterationBo.save(portalAlteration);
                        }
                    }
                } else {
                    System.out.println("\tThe study doesnot have any sequenced samples.");
                }


                DecimalFormat myFormatter = new DecimalFormat("##.##");
                String output = myFormatter.format(100 * (j + 1) / studyJSONResult.length());
                System.out.println("Importing " + output + "% done.");
            }

        }

    }
}
