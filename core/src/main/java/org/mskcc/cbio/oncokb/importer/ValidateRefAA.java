package org.mskcc.cbio.oncokb.importer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mskcc.cbio.oncokb.bo.OncokbTranscriptService;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ValidateRefAA {
    private static final Logger LOGGER = LogManager.getLogger();

    public void main(String[] args) throws ApiException, IOException {
        for (Alteration alteration : AlterationUtils.excludeVUS(new ArrayList<>(AlterationUtils.getAllAlterations()))) {
            checkAlteration(alteration, false);
        }

        for (Gene gene : CacheUtils.getAllGenes()) {
            for (Alteration alteration : CacheUtils.getVUS(gene.getEntrezGeneId())) {
                checkAlteration(alteration, true);
            }
        }
    }

    private String getNote(JSONObject jsonObj, ReferenceGenome referenceGenome) {
        List<String> note = new ArrayList<>();
        String rgStr = referenceGenome.equals(ReferenceGenome.GRCh37) ? "37" : "38";
        JSONArray jsonArray = jsonObj.getJSONArray("grch" + rgStr);
        note.add(jsonObj.getString("grch" + rgStr + "Note"));
        if (jsonArray.length() > 0) {
            note.add("Suggested variant");
            List<String> variants = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                variants.add(jsonArray.getString(i));
            }
            note.add(variants.stream().collect(Collectors.joining(", ")));
        }
        return note.stream().collect(Collectors.joining(" "));
    }

    private void checkAlteration(Alteration alteration, Boolean isVus) throws ApiException, IOException {
        if (alteration.getProteinStart() >= 0 && alteration.getReferenceGenomes() != null) {
            if (alteration.getReferenceGenomes() == null || alteration.getReferenceGenomes().size() == 0) {
                LOGGER.error("Alteration {} {} does not have reference genome", alteration.getGene().getHugoSymbol(), alteration.getName());
            } else {
                if (alteration.getRefResidues() != null) {
                    OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();
                    String referenceAA = oncokbTranscriptService.getAminoAcid(alteration.getReferenceGenomes().iterator().next(), alteration.getGene(), alteration.getProteinStart(), alteration.getRefResidues().length());
                    if (!referenceAA.equals(alteration.getRefResidues())) {
                        List<String> content = getBasicRecord(alteration);
                        content.add(String.valueOf(isVus));
                        content.add(alteration.getRefResidues());
                        content.add(referenceAA);
                        content.add("Reference amino acid does not match");

                        String response = null;
                        response = HttpUtils.getRequest("http://localhost:9090/api/suggest-variant/" + alteration.getGene().getHugoSymbol() + "?proteinPosition=" + alteration.getProteinStart() + "&curatedResidue=" + alteration.getRefResidues() + "&grch37Transcript=" + alteration.getGene().getGrch37Isoform() + "&grch38Transcript=" + alteration.getGene().getGrch38Isoform());

                        List<String> note = new ArrayList<>();
                        JSONObject jsonObj = new JSONObject(response);

                        note.add(getNote(jsonObj, ReferenceGenome.GRCh37));
                        note.add(getNote(jsonObj, ReferenceGenome.GRCh38));

                        content.add(note.stream().collect(Collectors.joining("; ")));
                        printContent(content);
                    }
                }
            }
        }
    }

    private static List<String> getBasicRecord(Alteration alteration) {
        List<String> content = new ArrayList<>();
        content.add("***");
        content.add(String.valueOf(alteration.getGene().getEntrezGeneId()));
        content.add(alteration.getGene().getGrch37Isoform());
        content.add(alteration.getGene().getGrch38Isoform());
        content.add(alteration.getGene().getHugoSymbol());
        content.add(alteration.getName());
        content.add(alteration.getAlteration());
        return content;
    }

    private static void printContent(List<String> content) {
        LOGGER.info(content.stream().collect(Collectors.joining("\t")));
    }
}
