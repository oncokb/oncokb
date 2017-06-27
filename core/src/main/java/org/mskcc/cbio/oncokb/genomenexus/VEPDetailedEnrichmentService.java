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
import org.springframework.stereotype.Service;

import java.util.List;

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

    public VariantAnnotation enrich(VariantAnnotation variantAnnotation) {
        if (variantAnnotation.getTranscriptConsequences() != null) {
            for (TranscriptConsequence transcriptConsequence : variantAnnotation.getTranscriptConsequences()) {
                transcriptConsequence.setCodonChange(resolveCodonChange(transcriptConsequence.getCodons()));
                transcriptConsequence.setConsequence(resolveConsequence(transcriptConsequence.getConsequenceTerms()));
                transcriptConsequence.setHgvspShort(resolveHgvspShort(transcriptConsequence.getHgvsp()));
                transcriptConsequence.setRefSeq(resolveRefSeq(transcriptConsequence.getRefseqTranscriptIds()));
            }
        }
        return variantAnnotation;
    }

    public String resolveHgvspShort(String hgvsp) {
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
