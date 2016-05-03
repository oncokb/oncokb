/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;
import org.mskcc.cbio.oncokb.util.GeneUtils;

/**
 *
 * @author jiaojiao April/14/2016 Import alteration data from portal database
 */
public class PortalAlterationImporter {

    public static List<Alteration> findAlterationList(Gene gene, String proteinChange, String mutation_type, Integer proteinStartPosition, Integer proteinEndPosition) {
        List<Alteration> alterations = new ArrayList<>();
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

        String[] consequences = mapper.get(mutation_type);
        for (String consequence : consequences) {
            alterations.addAll(AlterationUtils.getRelevantAlterations(gene, proteinChange, consequence, proteinStartPosition, proteinEndPosition));
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
        String joinedGenes = StringUtils.join(",", genes);

        String studies[] = {"skcm_tcga", "lusc_tcga", "luad_tcga", "coadread_tcga", "brca_tcga", "gbm_tcga", "hnsc_tcga", "kirc_tcga", "ov_tcga"};

        String joinedStudies = StringUtils.join(",", studies);
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
                    Set<Alteration> oncoKBAlterations = new HashSet<>(findAlterationList(gene, proteinChange, mutation_type, proteinStartPosition, proteinEndPosition));

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
