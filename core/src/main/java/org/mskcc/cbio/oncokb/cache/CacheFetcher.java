package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.OncoKBInfo;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CacheFetcher {
    @Cacheable(cacheResolver = "generalCacheResolver", key = "'all'")
    public OncoKBInfo getOncoKBInfo() {
        return new OncoKBInfo();
    }

    @Cacheable(cacheResolver = "generalCacheResolver", key = "'all'")
    public List<CancerGene> getCancerGenes() {
        return CancerGeneUtils.getCancerGeneList();
    }

    @Cacheable(cacheResolver = "generalCacheResolver", key = "'all'")
    public String getCancerGenesTxt() {

        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("Hugo Symbol");
        header.add("Entrez Gene ID");
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("# of occurrence within resources (Column D-J)");
        header.add("OncoKB Annotated");
        header.add("Is Oncogene");
        header.add("Is Tumor Suppressor Gene");
        header.add("MSK-IMPACT");
        header.add("MSK-HEME");
        header.add("FOUNDATION ONE");
        header.add("FOUNDATION ONE HEME");
        header.add("Vogelstein");
        header.add("SANGER CGC(05/30/2017)");
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        for (CancerGene cancerGene : CancerGeneUtils.getCancerGeneList()) {
            List<String> row = new ArrayList<>();
            row.add(cancerGene.getHugoSymbol());
            row.add(cancerGene.getEntrezGeneId().toString());
            row.add(cancerGene.getGrch37Isoform());
            row.add(cancerGene.getGrch37RefSeq());
            row.add(cancerGene.getGrch38Isoform());
            row.add(cancerGene.getGrch37RefSeq());
            row.add(String.valueOf(cancerGene.getOccurrenceCount()));
            row.add(getStringByBoolean(cancerGene.getOncokbAnnotated()));
            row.add(getStringByBoolean(cancerGene.getOncogene()));
            row.add(getStringByBoolean(cancerGene.getTSG()));
            row.add(getStringByBoolean(cancerGene.getmSKImpact()));
            row.add(getStringByBoolean(cancerGene.getmSKHeme()));
            row.add(getStringByBoolean(cancerGene.getFoundation()));
            row.add(getStringByBoolean(cancerGene.getFoundationHeme()));
            row.add(getStringByBoolean(cancerGene.getVogelstein()));
            row.add(getStringByBoolean(cancerGene.getSangerCGC()));
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }
        return sb.toString();
    }


    @Cacheable(cacheResolver = "generalCacheResolver")
    public List<CuratedGene> getCuratedGenes(boolean includeEvidence) {
        List<CuratedGene> genes = new ArrayList<>();
        for (Gene gene : CacheUtils.getAllGenes()) {
            // Skip all genes without entrez gene id
            if (gene.getEntrezGeneId() == null) {
                continue;
            }

            String highestSensitiveLevel = "";
            String highestResistanceLevel = "";
            Set<Evidence> therapeuticEvidences = EvidenceUtils.getEvidenceByGeneAndEvidenceTypes(gene, EvidenceTypeUtils.getTreatmentEvidenceTypes());
            Set<Evidence> highestSensitiveLevelEvidences = EvidenceUtils.getOnlyHighestLevelEvidences(EvidenceUtils.getSensitiveEvidences(therapeuticEvidences), null, null);
            Set<Evidence> highestResistanceLevelEvidences = EvidenceUtils.getOnlyHighestLevelEvidences(EvidenceUtils.getResistanceEvidences(therapeuticEvidences), null, null);
            if (!highestSensitiveLevelEvidences.isEmpty()) {
                highestSensitiveLevel = highestSensitiveLevelEvidences.iterator().next().getLevelOfEvidence().getLevel();
            }
            if (!highestResistanceLevelEvidences.isEmpty()) {
                highestResistanceLevel = highestResistanceLevelEvidences.iterator().next().getLevelOfEvidence().getLevel();
            }

            genes.add(
                new CuratedGene(
                    gene.getGrch37Isoform(), gene.getGrch37RefSeq(),
                    gene.getGrch38Isoform(), gene.getGrch38RefSeq(),
                    gene.getEntrezGeneId(), gene.getHugoSymbol(),
                    gene.getTSG(), gene.getOncogene(),
                    highestSensitiveLevel, highestResistanceLevel,
                    includeEvidence ? SummaryUtils.geneSummary(gene, gene.getHugoSymbol()) : "",
                    includeEvidence ? SummaryUtils.geneBackground(gene, gene.getHugoSymbol()) : ""
                )
            );
        }
        MainUtils.sortCuratedGenes(genes);
        return genes;
    }

    @Cacheable(cacheResolver = "generalCacheResolver")
    public String getCuratedGenesTxt(boolean includeEvidence) {
        String separator = "\t";
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        List<String> header = new ArrayList<>();
        header.add("GRCh37 Isoform");
        header.add("GRCh37 RefSeq");
        header.add("GRCh38 Isoform");
        header.add("GRCh38 RefSeq");
        header.add("Entrez Gene ID");
        header.add("Hugo Symbol");
        header.add("Is Oncogene");
        header.add("Is Tumor Suppressor Gene");
        header.add("Highest Level of Evidence(sensitivity)");
        header.add("Highest Level of Evidence(resistance)");
        if (includeEvidence == Boolean.TRUE) {
            header.add("Summary");
            header.add("Background");
        }
        sb.append(MainUtils.listToString(header, separator));
        sb.append(newLine);

        List<CuratedGene> genes = this.getCuratedGenes(includeEvidence == Boolean.TRUE);
        for (CuratedGene gene : genes) {
            List<String> row = new ArrayList<>();
            row.add(gene.getGrch37Isoform());
            row.add(gene.getGrch37RefSeq());
            row.add(gene.getGrch38Isoform());
            row.add(gene.getGrch38RefSeq());
            row.add(String.valueOf(gene.getEntrezGeneId()));
            row.add(gene.getHugoSymbol());
            row.add(getStringByBoolean(gene.getOncogene()));
            row.add(getStringByBoolean(gene.getTSG()));
            row.add(gene.getHighestSensitiveLevel());
            row.add(gene.getHighestResistancLevel());
            if (includeEvidence == Boolean.TRUE) {
                row.add(gene.getSummary());
                row.add(gene.getBackground());
            }
            sb.append(MainUtils.listToString(row, separator));
            sb.append(newLine);
        }
        return sb.toString();
    }

    private String getStringByBoolean(Boolean val) {
        return val ? "Yes" : "No";
    }
}
