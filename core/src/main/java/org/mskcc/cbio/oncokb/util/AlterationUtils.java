/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.oncokb.bo.VariantConsequenceBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.VariantConsequence;

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
                    p = Pattern.compile("([A-Z]+)([0-9]+)((ins)|(del))");
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
}
