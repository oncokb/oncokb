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
    
    public static void annotateAlteration(Alteration alteration) {
        String consequence = "N/A";
        String ref = null;
        String var = null;
        Integer start = -1;
        Integer end = 100000;
        
        String alt = alteration.getAlteration();
        if (alt.startsWith("p.")) {
            alt = alt.substring(2);
        }
        
        Pattern p = Pattern.compile("([A-Z\\*])([0-9]+)([A-Z\\*]?)");
        Matcher m = p.matcher(alt);
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
            m = p.matcher(alt);
            if (m.matches()) {
                start = Integer.valueOf(m.group(1));
                end = Integer.valueOf(m.group(2));
                String v = m.group(3);
                if (v.equals("mis")) {
                    consequence = "missense_variant";
                } else if (v.equals("ins")) {
                    consequence = "inframe_insertion";
                } else if (v.equals("del")) {
                    consequence = "inframe_deletion";
                } else if (v.equals("fs")) {
                    consequence = "frameshift_variant";
                } else if (v.equals("trunc")) {
                    consequence = "feature_truncation";
                }
            } else {
                p = Pattern.compile("([A-Z\\*])([0-9]+)fs.*");
                m = p.matcher(alt);
                if (m.matches()) {
                    ref = m.group(1);
                    start = Integer.valueOf(m.group(2));
                    end = start;

                    consequence = "frameshift_variant";
                }
            }
        }
        
        // truncating
        if (alt.toLowerCase().matches("truncating mutations?")) {
            consequence = "feature_truncation";
        }
        
        VariantConsequenceBo variantConsequenceBo = ApplicationContextSingleton.getVariantConsequenceBo();
        VariantConsequence variantConsequence = variantConsequenceBo.findVariantConsequenceByTerm(consequence);
        
        if (ref!=null && !ref.isEmpty()) {
            alteration.setRefResidues(ref);
        }
        
        if (var!=null && !var.isEmpty()) {
            alteration.setVariantResidues(var);
        }
        
        if (start!=null) {
            alteration.setProteinStart(start);
        }
        
        if (end!=null) {
            alteration.setProteinEnd(end);
        }
        
        if (variantConsequence!=null) {
            alteration.setConsequence(variantConsequence);
        }
    }
}
