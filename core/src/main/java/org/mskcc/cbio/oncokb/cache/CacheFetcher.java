package org.mskcc.cbio.oncokb.cache;

import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.apiModels.FdaAlteration;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotationQueryType;
import org.mskcc.cbio.oncokb.bo.OncokbTranscriptService;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.oncokb.oncokb_transcript.ApiException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.mskcc.cbio.oncokb.Constants.DEFAULT_REFERENCE_GENOME;
import static org.mskcc.cbio.oncokb.model.InferredMutation.ONCOGENIC_MUTATIONS;

@Component
public class CacheFetcher {
    OncokbTranscriptService oncokbTranscriptService = new OncokbTranscriptService();

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

    @Cacheable(cacheResolver = "generalCacheResolver")
    public Gene findGeneBySymbol(String symbol) throws ApiException {
        return this.oncokbTranscriptService.findGeneBySymbol(symbol);
    }

    @Cacheable(cacheResolver = "generalCacheResolver", key = "'all'")
    public Set<FdaAlteration> getAllFdaAlterations() {
        Set<Gene> genes = CacheUtils.getAllGenes();
        Set<FdaAlteration> alterations = new HashSet<>();
        Map<Alteration, Map<String, Set<FdaAlteration>>> resultMap = new HashMap<>();
        for (Gene gene : genes) {
            for (ClinicalVariant clinicalVariant : MainUtils.getClinicalVariants(gene)) {
                LevelOfEvidence level = LevelOfEvidence.getByLevel(clinicalVariant.getLevel());
                if (level == null || !LevelUtils.getTherapeuticLevels().contains(level)) {
                    continue;
                }
                Set<Alteration> mappedAlterations = new HashSet<>();
                if (clinicalVariant.getVariant().getAlteration().equals(ONCOGENIC_MUTATIONS.getVariant())) {
                    for (BiologicalVariant annotatedAlt : MainUtils.getBiologicalVariants(gene)) {
                        Oncogenicity oncogenicity = Oncogenicity.getByEffect(annotatedAlt.getOncogenic());
                        if (MainUtils.isOncogenic(oncogenicity)) {
                            mappedAlterations.add(annotatedAlt.getVariant());
                        }
                    }
                } else {
                    mappedAlterations.add(clinicalVariant.getVariant());
                }
                for (Alteration alteration : mappedAlterations) {
                    if (!resultMap.containsKey(alteration)) {
                        resultMap.put(alteration, new HashMap<>());
                    }
                    for (TumorType tumorType : clinicalVariant.getCancerTypes()) {
                        FdaAlteration fdaAlteration = new FdaAlteration();
                        String cancerTypeName = TumorTypeUtils.getTumorTypeName(tumorType);
                        fdaAlteration.setAlteration(alteration);
                        fdaAlteration.setLevel(convertToFdaLevel(LevelOfEvidence.getByLevel(clinicalVariant.getLevel()), clinicalVariant));
                        fdaAlteration.setCancerType(cancerTypeName);
                        if (!resultMap.get(alteration).containsKey(cancerTypeName)) {
                            resultMap.get(alteration).put(cancerTypeName, new HashSet<>());
                        }
                        resultMap.get(alteration).get(cancerTypeName).add(fdaAlteration);
                    }
                }
            }
        }

        for (Map.Entry<Alteration, Map<String, Set<FdaAlteration>>> alterationMap : resultMap.entrySet()) {
            for (Map.Entry<String, Set<FdaAlteration>> cancerTypeMap : alterationMap.getValue().entrySet()) {
                FdaAlteration pickedFdaAlt = cancerTypeMap.getValue().iterator().next();
                if (cancerTypeMap.getValue().size() > 1) {
                    Optional<FdaAlteration> level2 = cancerTypeMap.getValue().stream().filter(fdaAlteration -> fdaAlteration.getLevel().equals(FDA_L_2)).findAny();
                    if (level2.isPresent()) {
                        pickedFdaAlt = level2.get();
                    }
                }
                alterations.add(pickedFdaAlt);
            }
        }
        return alterations;
    }

    final String FDA_L_2 = "FDAx2";
    final String FDA_L_3 = "FDAx3";
    private String convertToFdaLevel(LevelOfEvidence level, ClinicalVariant clinicalVariant) {
        if (level == null) {
            return "";
        }
        switch (level) {
            case LEVEL_1:
            case LEVEL_R1:
                return FDA_L_2;
            case LEVEL_2:
                if (specialFdaL3(clinicalVariant)) {
                    return FDA_L_3;
                } else {
                    return FDA_L_2;
                }
            case LEVEL_3A:
            case LEVEL_4:
            case LEVEL_R2:
                return FDA_L_3;
            default:
                return "";
        }
    }

    private boolean specialFdaL3(ClinicalVariant clinicalVariant) {
        List<String> specialList = Arrays.asList(new String[]{"ERBB2&Oncogenic Mutations&Non-Small Cell Lung Cancer&Ado-Trastuzumab Emtansine", "EGFR&A763_Y764insFQEA&Non-Small Cell Lung Cancer&Erlotinib", "ALK&Fusions&Inflammatory Myofibroblastic Tumor&Crizotinib", "ALK&Fusions&Inflammatory Myofibroblastic Tumor&Ceritinib", "BRAF&V600E&Ganglioglioma, Pleomorphic Xanthoastrocytoma, Pilocytic Astrocytoma&Cobimetinib+Vemurafenib,", "BRAF&V600E&Ganglioglioma, Pleomorphic Xanthoastrocytoma, Pilocytic Astrocytoma&Trametinib+Dabrafenib"});

        String separator = "&";
        List<String> queryParts = new ArrayList<>();
        queryParts.add(clinicalVariant.getVariant().getGene().getHugoSymbol());
        queryParts.add(clinicalVariant.getVariant().getAlteration());
        queryParts.add(TumorTypeUtils.getTumorTypesName(clinicalVariant.getCancerTypes()));
        queryParts.add(String.join("+", clinicalVariant.getDrug().stream().sorted().collect(Collectors.toList())));
        String query = String.join(separator, queryParts);
        return specialList.contains(query);
    }

    @Cacheable(
        cacheResolver = "generalCacheResolver",
        keyGenerator = "concatKeyGenerator"
    )
    public IndicatorQueryResp processQuery(ReferenceGenome referenceGenome,
                                           Integer entrezGeneId,
                                           String hugoSymbol,
                                           String alteration,
                                           String alterationType,
                                           String tumorType,
                                           String consequence,
                                           Integer proteinStart,
                                           Integer proteinEnd,
                                           StructuralVariantType svType,
                                           String hgvs,
                                           Set<LevelOfEvidence> levels,
                                           Boolean highestLevelOnly,
                                           Set<EvidenceType> evidenceTypes) {
        if (referenceGenome == null) {
            referenceGenome = DEFAULT_REFERENCE_GENOME;
        }
        Query query = new Query(null, referenceGenome, AnnotationQueryType.REGULAR.getName(), entrezGeneId, hugoSymbol, alteration, alterationType, svType, tumorType, consequence, proteinStart, proteinEnd, hgvs);
        return IndicatorUtils.processQuery(
            query, levels, highestLevelOnly,
            evidenceTypes
        );
    }

    @Cacheable(cacheResolver = "generalCacheResolver",
        keyGenerator = "concatKeyGenerator")
    public Alteration getAlterationFromGenomeNexus(GNVariantAnnotationType gnVariantAnnotationType, ReferenceGenome referenceGenome, String genomicLocation) {
        return AlterationUtils.getAlterationFromGenomeNexus(gnVariantAnnotationType, genomicLocation, referenceGenome);
    }
}
