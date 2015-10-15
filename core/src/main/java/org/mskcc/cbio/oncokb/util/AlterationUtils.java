/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mskcc.cbio.oncokb.bo.AlterationBo;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.bo.VariantConsequenceBo;
import org.mskcc.cbio.oncokb.model.*;

/**
 *
 * @author jgao
 */
public final class AlterationUtils {
    private AlterationUtils() {
        throw new AssertionError();
    }
    
    public static void annotateAlteration(Alteration alteration, String proteinChange) {
        String consequence = "N/A";
        String ref = null;
        String var = null;
        Integer start = -1;
        Integer end = 100000;
        
        if (proteinChange.startsWith("p.")) {
            proteinChange = proteinChange.substring(2);
        }
        
        if (proteinChange.indexOf("[")!=-1) {
            proteinChange = proteinChange.substring(0, proteinChange.indexOf("["));
        }
        
        proteinChange = proteinChange.trim();
        
        Pattern p = Pattern.compile("([A-Z\\*])([0-9]+)([A-Z\\*]?)");
        Matcher m = p.matcher(proteinChange);
        if (m.matches()) {
            ref = m.group(1);
            start = Integer.valueOf(m.group(2));
            end = start;
            var = m.group(3);
            
            if (ref.equals(var)) {
                consequence = "synonymous_variant";
            } else if (ref.equals("*")) {
                consequence = "stop_lost";
            } else if (var.equals("*")) {
                consequence = "stop_gained";
            } else if (start==1) {
                consequence = "initiator_codon_variant";
            } else {
                consequence = "missense_variant";
            }
        } else {
            p = Pattern.compile("[A-Z]?([0-9]+)_[A-Z]?([0-9]+)(.+)");
            m = p.matcher(proteinChange);
            if (m.matches()) {
                start = Integer.valueOf(m.group(1));
                end = Integer.valueOf(m.group(2));
                String v = m.group(3);
                switch (v) {
                    case "mis":
                        consequence = "missense_variant";
                        break;
                    case "ins":
                        consequence = "inframe_insertion";
                        break;
                    case "del":
                        consequence = "inframe_deletion";
                        break;
                    case "fs":
                        consequence = "frameshift_variant";
                        break;
                    case "trunc":
                        consequence = "feature_truncation";
                        break;
                    case "mut":
                        consequence = "any";
                }
            } else {
                p = Pattern.compile("([A-Z\\*])([0-9]+)[A-Z]?fs.*");
                m = p.matcher(proteinChange);
                if (m.matches()) {
                    ref = m.group(1);
                    start = Integer.valueOf(m.group(2));
                    end = start;

                    consequence = "frameshift_variant";
                }else {
                    p = Pattern.compile("([A-Z]+)?([0-9]+)((ins)|(del))");
                    m = p.matcher(proteinChange);
                    if (m.matches()) {
                        ref = m.group(1);
                        start = Integer.valueOf(m.group(2));
                        end = start;
                        String v = m.group(3);
                        switch (v) {
                            case "ins":
                                consequence = "inframe_insertion";
                                break;
                            case "del":
                                consequence = "inframe_deletion";
                                break;
                        }
                    }
                }
            }
        }
        
        // truncating
        if (proteinChange.toLowerCase().matches("truncating mutations?")) {
            consequence = "feature_truncation";
        }
        
        VariantConsequenceBo variantConsequenceBo = ApplicationContextSingleton.getVariantConsequenceBo();
        VariantConsequence variantConsequence = variantConsequenceBo.findVariantConsequenceByTerm(consequence);
        
        if (alteration.getRefResidues()==null && ref!=null && !ref.isEmpty()) {
            alteration.setRefResidues(ref);
        }
        
        if (alteration.getVariantResidues()==null && var!=null && !var.isEmpty()) {
            alteration.setVariantResidues(var);
        }
        
        if (alteration.getProteinStart()==null && start!=null) {
            alteration.setProteinStart(start);
        }
        
        if (alteration.getProteinEnd()==null && end!=null) {
            alteration.setProteinEnd(end);
        }
        
        if (alteration.getConsequence()==null && variantConsequence!=null) {
            alteration.setConsequence(variantConsequence);
        }
    }

    public static String getVariantName(String gene, String alteration) {
        //Gene + mutation name
        String variantName = "";

        if(gene != null && alteration != null && alteration.toLowerCase().contains(gene.toLowerCase())) {
            variantName = alteration;
        }else {
            variantName = (gene != null ? (gene+" " ) : "") + (alteration != null ? alteration : "");
        }

        if(alteration != null) {
            if(alteration.toLowerCase().contains("fusion")){
//            variantName = variantName.concat(" event");
            }else if(alteration.toLowerCase().contains("deletion") || alteration.toLowerCase().contains("amplification")){
                //Keep the variant name
            }else{
                variantName = variantName.concat(" mutation");
            }
        }
        return variantName;
    }

    public static String trimAlterationName(String alteration) {
        if (alteration!=null) {
            if (alteration.startsWith("p.")) {
                alteration = alteration.substring(2);
            }
        }
        return alteration;
    }

    public static Alteration getAlteration(String hugoSymbol, String alteration, String alterationType, String consequence, Integer proteinStart, Integer proteinEnd) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Alteration alt = new Alteration();

        if (alteration!=null) {
            alteration = AlterationUtils.trimAlterationName(alteration);
            alt.setAlteration(alteration);
        }

        Gene gene = null;
        if (hugoSymbol!=null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }
        alt.setGene(gene);

        AlterationType type = AlterationType.MUTATION;
        if (alterationType != null) {
            AlterationType t = AlterationType.valueOf(alterationType.toUpperCase());
            if (t!=null) {
                type = t;
            }
        }
        alt.setAlterationType(type);

        VariantConsequence variantConsequence = null;
        if (consequence!=null) {
            variantConsequence = ApplicationContextSingleton.getVariantConsequenceBo().findVariantConsequenceByTerm(consequence);
        }
        alt.setConsequence(variantConsequence);

        if (proteinEnd==null) {
            proteinEnd = proteinStart;
        }
        alt.setProteinStart(proteinStart);
        alt.setProteinEnd(proteinEnd);

        AlterationUtils.annotateAlteration(alt, alt.getAlteration());
        return alt;
    }
}
