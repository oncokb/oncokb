/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;

/**
 * @author jiaojiao Sep/8/2017 Import alteration data from portal database
 */
public class MapPortalAlteration {
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
        mapper.put("Missense", new String[]{MISSENSE_VARIANT});
        mapper.put("Missense_Mutation", new String[]{MISSENSE_VARIANT});
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
                alterations.addAll(AlterationUtils.getRelevantAlterations(ReferenceGenome.GRCh37, alt));
            }
            alterationsSet = AlterationUtils.excludeVUS(alterations);
        }

        return alterationsSet;
    }

    public static void main(String[] args) throws IOException {
        for (PortalAlteration portalAlteration : ApplicationContextSingleton.getPortalAlterationBo().findAll()) {
            List<Alteration> oncoKBAlterations = findAlterationList(portalAlteration.getGene(), portalAlteration.getProteinChange(), portalAlteration.getAlterationType(), portalAlteration.getProteinStartPosition(), portalAlteration.getProteinEndPosition());
            for (Alteration oncoKBAlteration : oncoKBAlterations) {
                Set<PortalAlteration> portalAlterations = oncoKBAlteration.getPortalAlterations();
                portalAlterations.add(portalAlteration);

                oncoKBAlteration.setPortalAlterations(portalAlterations);
                ApplicationContextSingleton.getAlterationBo().update(oncoKBAlteration);
            }
        }
        System.out.println("Finished.");
    }
}
