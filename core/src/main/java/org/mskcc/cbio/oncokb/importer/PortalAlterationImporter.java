/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;

/**
 *
 * @author jiaojiao April/14/2016 Import alteration data from portal database
 */
public class PortalAlterationImporter {

    public static List<Alteration> findAlterationList(String hugo_gene_symbol, String proteinChange, String mutation_type, Integer proteinStartPosition, Integer proteinEndPosition) {
        AlterationBo alterationBo = ApplicationContextSingleton.getAlterationBo();
        Alteration alteration, alteration1 = new Alteration(), alteration2, alteration3;
        List<Alteration> alterations = null, alterationList = new ArrayList<>();
        Boolean flag = false;
        if (mutation_type.equals("Targeted_Region") || mutation_type.equals("COMPLEX_INDEL")) {
            alteration1 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "inframe_deletion", proteinStartPosition, proteinEndPosition);
            alteration2 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "inframe_insertion", proteinStartPosition, proteinEndPosition);

            alterationList.add(alteration2);

        } else if (mutation_type.equals("Indel")) {
            alteration1 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "frameshift_variant", proteinStartPosition, proteinEndPosition);
            alteration2 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "inframe_deletion", proteinStartPosition, proteinEndPosition);
            alteration3 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "inframe_insertion", proteinStartPosition, proteinEndPosition);

            alterationList.add(alteration2);
            alterationList.add(alteration3);

        } else if (mutation_type.equals("In_Frame_Del")) {
            alteration1 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "inframe_deletion", proteinStartPosition, proteinEndPosition);
            alteration2 = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", "feature_truncation", proteinStartPosition, proteinEndPosition);

            alterationList.add(alteration2);

        } else {
            flag = true;
        }

        if (!flag) {
            alterations = alterationBo.findRelevantAlterations(alteration1, alterationList);
        } else {
            String consequence = "";
            switch (mutation_type) {
                case "3'Flank":
                    consequence = "any";
                    break;
                case "5'Flank":
                    consequence = "any";
                    break;
                case "ESSENTIAL_SPLICE_SITE":
                    consequence = "feature_truncation";
                    break;
                case "Exon skipping":
                    consequence = "inframe_deletion";
                    break;
                case "Frameshift deletion":
                    consequence = "frameshift_variant";
                    break;
                case "Frameshift insertion":
                    consequence = "frameshift_variant";
                    break;
                case "FRAMESHIFT_CODING":
                    consequence = "frameshift_variant";
                    break;
                case "Frame_Shift_Del":
                    consequence = "frameshift_variant";
                case "Frame_Shift_Ins":
                    consequence = "frameshift_variant";
                case "Fusion":
                    consequence = "fusion";
                    break;
                case "In_Frame_Ins":
                    consequence = "inframe_insertion";
                    break;
                case "Missense":
                    consequence = "missense_variant";
                    break;
                case "Missense_Mutation":
                    consequence = "missense_variant";
                    break;
                case "Nonsense_Mutation":
                    consequence = "stop_gained";
                    break;
                case "Nonstop_Mutation":
                    consequence = "stop_lost";
                    break;
                case "Splice_Site":
                    consequence = "splice_region_variant";
                    break;
                case "Splice_Site_Del":
                    consequence = "splice_region_variant";
                    break;
                case "Splice_Site_SNP":
                    consequence = "splice_region_variant";
                    break;
                case "splicing":
                    consequence = "splice_region_variant";
                    break;
                case "Translation_Start_Site":
                    consequence = "start_lost";
                    break;
                case "vIII deletion":
                    consequence = "any";
                    break;
            }
            alteration = AlterationUtils.getAlteration(hugo_gene_symbol, proteinChange, "MUTATION", consequence, null, null);
            alterations = alterationBo.findRelevantAlterations(alteration, null);
        }

        return alterations;
    }

    public static void main(String[] args) throws IOException {
        JSONObject jObject = null;
        PortalAlteration portalAlteration = null;

        String geneUrl = "http://oncokb.org/gene.json";
        String geneResult = FileUtils.readRemote(geneUrl);
        JSONArray geneJSONResult = new JSONArray(geneResult);
        String genes[] = new String[geneJSONResult.length()];
        for (int i = 0; i < geneJSONResult.length(); i++) {
            jObject = geneJSONResult.getJSONObject(i);
            genes[i] = jObject.get("hugoSymbol").toString();

        }

        String joinedGenes = String.join(",", genes);

        String studies[] = {"skcm_tcga", "lusc_tcga", "luad_tcga", "coadread_tcga", "brca_tcga", "gbm_tcga", "hnsc_tcga", "kirc_tcga", "ov_tcga"};

        String joinedStudies = String.join(",", studies);
        String studyUrl = "http://www.cbioportal.org/api/studies?study_ids=" + joinedStudies;
        String studyResult = FileUtils.readRemote(studyUrl);
        JSONArray studyJSONResult = new JSONArray(studyResult);

        for (int j = 0; j < studyJSONResult.length(); j++) {
            jObject = studyJSONResult.getJSONObject(j);
            if (jObject.has("id") && jObject.has("type_of_cancer")) {
                String cancerStudy = jObject.get("id").toString();
                String cancerType = jObject.get("type_of_cancer").toString();
                System.out.println("*****************************************************************************");
                System.out.println("Importing portal alteration data for " + jObject.get("name").toString());

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
                    String sampleId = jObject.getString("sample_id");
                    GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
                    Gene gene = geneBo.findGeneByHugoSymbol(hugo_gene_symbol);

                    Set<Alteration> oncoKBAlterations = new HashSet<>(findAlterationList(hugo_gene_symbol, proteinChange, mutation_type, proteinStartPosition, proteinEndPosition));

                    PortalAlterationBo portalAlterationBo = ApplicationContextSingleton.getPortalAlterationBo();
                    portalAlteration = new PortalAlteration(cancerType, cancerStudy, sampleId, gene, proteinChange, proteinStartPosition, proteinEndPosition, oncoKBAlterations, mutation_type);
                    portalAlterationBo.save(portalAlteration);

                }
                DecimalFormat myFormatter = new DecimalFormat("##.##");
                String output = myFormatter.format(100 * (j + 1) / studyJSONResult.length());
                System.out.println("Importing " + output + "% done.");
            }

        }

    }
}
