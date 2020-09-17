/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.oncokb.genomenexus;

import org.apache.commons.lang3.StringUtils;
import org.genome_nexus.client.TranscriptConsequence;
import org.genome_nexus.client.VariantAnnotation;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Benjamin Gross
 * @author Hongxin Zhang
 */
@Service
public class VEPDetailedEnrichmentService {

    private String aa3to1[][] = {
        {"Ala", "A"}, {"Arg", "R"}, {"Asn", "N"}, {"Asp", "D"}, {"Asx", "B"}, {"Cys", "C"},
        {"Glu", "E"}, {"Gln", "Q"}, {"Glx", "Z"}, {"Gly", "G"}, {"His", "H"}, {"Ile", "I"},
        {"Leu", "L"}, {"Lys", "K"}, {"Met", "M"}, {"Phe", "F"}, {"Pro", "P"}, {"Ser", "S"},
        {"Thr", "T"}, {"Trp", "W"}, {"Tyr", "Y"}, {"Val", "V"}, {"Xxx", "X"}, {"Ter", "*"}
    };
    private Set<String> spliceSiteVariants = new HashSet<String>(Arrays.asList(
        "splice_acceptor_variant", "splice_donor_variant", "splice_region_variant"));
    private Pattern cDnaExtractor = Pattern.compile(".*[cn].-?\\*?(\\d+).*");

    public VariantAnnotation enrich(VariantAnnotation variantAnnotation) {
        if (variantAnnotation.getTranscriptConsequences() != null) {
            for (TranscriptConsequence transcriptConsequence : variantAnnotation.getTranscriptConsequences()) {
                transcriptConsequence.setCodons(transcriptConsequence.getCodons());
                transcriptConsequence.setConsequenceTerms(transcriptConsequence.getConsequenceTerms());
                transcriptConsequence.setHgvsp(resolveHgvspShort(transcriptConsequence));
                transcriptConsequence.setRefseqTranscriptIds(transcriptConsequence.getRefseqTranscriptIds());
            }
        }
        return variantAnnotation;
    }

    public String resolveHgvspShort(TranscriptConsequence transcriptConsequence) {
        String hgvsp = "";
        if (transcriptConsequence != null) {
            if (transcriptConsequence.getHgvsp() != null) {
                hgvsp = getProcessedHgvsp(transcriptConsequence.getHgvsp());
                for (int i = 0; i < 24; i++) {
                    if (hgvsp.contains(aa3to1[i][0])) {
                        hgvsp = hgvsp.replaceAll(aa3to1[i][0], aa3to1[i][1]);
                    }
                }
            } else if (transcriptConsequence.getHgvsc() != null && spliceSiteVariants.contains(transcriptConsequence.getConsequenceTerms().get(0))) {
                Integer cPos = 0;
                Integer pPos = 0;
                Matcher m = cDnaExtractor.matcher(transcriptConsequence.getHgvsc());
                if (m.matches()) {
                    cPos = Integer.parseInt(m.group(1));
                    cPos = cPos < 1 ? 1 : cPos;
                    pPos = (int)Math.ceil(((cPos + cPos % 3) / 3.0));
                    hgvsp = "p.X" + String.valueOf(pPos) + "_splice";
                }
            } else {
                // try to salvage using protein_start, amino_acids, and consequence_terms
                hgvsp = resolveHgvspShortFromAAs(transcriptConsequence);
            }
        }

        if (hgvsp != null) {
            hgvsp = getProcessedHgvsp(hgvsp);
            for (int i = 0; i < 24; i++) {
                if (hgvsp.contains(aa3to1[i][0])) {
                    hgvsp = hgvsp.replaceAll(aa3to1[i][0], aa3to1[i][1]);
                }
            }
        }
        return hgvsp;
    }

    private String resolveHgvspShortFromAAs(TranscriptConsequence transcriptConsequence) {
        String hgvsp = "";
        try {
            String[] aaParts = transcriptConsequence.getAminoAcids().split("/");
            if (transcriptConsequence.getConsequenceTerms() != null && transcriptConsequence.getConsequenceTerms().get(0).equals("inframe_insertion")) {
                hgvsp = aaParts[1].substring(0, 1) + transcriptConsequence.getProteinStart() + "_" + aaParts[1].substring(1, 2) + "ins" +
                    transcriptConsequence.getProteinEnd() + aaParts[1].substring(2);
            } else if (transcriptConsequence.getConsequenceTerms() != null && transcriptConsequence.getConsequenceTerms().get(0).equals("inframe_deletion")) {
                hgvsp = aaParts[0] + "del";
            } else {
                hgvsp = aaParts[0] + transcriptConsequence.getProteinStart();
                if (transcriptConsequence.getConsequenceTerms() != null && transcriptConsequence.getConsequenceTerms().get(0).equals("frameshift_variant")) {
                    hgvsp += "fs";
                } else {
                    hgvsp += aaParts[1];
                }
            }
        } catch (NullPointerException e) {
        }

        return hgvsp;
    }

    public String resolveRefSeq(List<String> refSeqTranscriptIds) {
        String refSeq = "";
        if (refSeqTranscriptIds != null) {
            if (refSeqTranscriptIds.size() > 0) {
                refSeq = refSeqTranscriptIds.get(0).trim();
            }
        }
        return refSeq != null ? refSeq : "";
    }

    public String resolveCodonChange(String codons) {
        String codonChange = "";
        if (codons != null) {
            codonChange = codons;
        }

        return codonChange != null ? codonChange : "";
    }


    public String resolveConsequence(List<String> consequenceTerms) {
        if (consequenceTerms == null) {
            return "";
        }
        if (consequenceTerms != null && consequenceTerms.size() > 0) {
            return StringUtils.join(consequenceTerms, ",");
        }
        return "";
    }

    private String getProcessedHgvsp(String hgvsp) {
        int iHgvsp = hgvsp.indexOf(":");
        if (hgvsp.contains("(p.%3D)")) {
            return "p.=";
        } else {
            return hgvsp.substring(iHgvsp + 1);
        }
    }
}
