package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.apiModels.ActionableGene;
import org.mskcc.cbio.oncokb.apiModels.AnnotatedVariant;
import org.mskcc.cbio.oncokb.apiModels.Citations;
import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.model.tumor_type.TumorType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Hongxin Zhang on 4/5/16.
 */
public class MainUtils {
    static String DataVersion = null;
    static String DataVersionDate = null;
    private static final List<Oncogenicity> PRIORITIZED_ONCOGENICITY = Collections.unmodifiableList(
        Arrays.asList(
            Oncogenicity.UNKNOWN,
            Oncogenicity.INCONCLUSIVE,
            Oncogenicity.LIKELY_NEUTRAL,
            Oncogenicity.PREDICTED,
            Oncogenicity.LIKELY,
            Oncogenicity.YES)
    );
    private static final List<MutationEffect> PRIORITIZED_MUTATION_EFFECTS = Collections.unmodifiableList(
        Arrays.asList(MutationEffect.GAIN_OF_FUNCTION,
            MutationEffect.LIKELY_GAIN_OF_FUNCTION,
            MutationEffect.INCONCLUSIVE,
            MutationEffect.LIKELY_NEUTRAL,
            MutationEffect.NEUTRAL,
            MutationEffect.LIKELY_SWITCH_OF_FUNCTION,
            MutationEffect.SWITCH_OF_FUNCTION,
            MutationEffect.LIKELY_LOSS_OF_FUNCTION,
            MutationEffect.LOSS_OF_FUNCTION,
            MutationEffect.UNKNOWN
        )
    );

    public static boolean isEGFRTruncatingVariants(String alteration) {
        return alteration == null ? false : (alteration.trim().matches("^v(II|III|IV(a|b|c)|V)?$"));
    }

    public static Map<String, Object> GetRequestQueries(
        String entrezGeneId, String hugoSymbol, ReferenceGenome referenceGenome, String alteration, String tumorType,
        String evidenceType, String consequence, String proteinStart, String proteinEnd,
        String levels) {

        Map<String, Object> requestQueries = new HashMap<>();

        List<Query> queries = new ArrayList<>();
        List<EvidenceType> evidenceTypes = new ArrayList<>();
        List<LevelOfEvidence> levelOfEvidences = new ArrayList<>();
        String[] genes = {};

        if (entrezGeneId != null) {
            for (String id : entrezGeneId.trim().split("\\s*,\\s*")) {
                Query requestQuery = new Query();
                requestQuery.setEntrezGeneId(Integer.parseInt(id));
                queries.add(requestQuery);
            }
        } else if (hugoSymbol != null) {
            for (String symbol : hugoSymbol.trim().split("\\s*,\\s*")) {
                Query requestQuery = new Query();
                if (symbol.equals(SpecialStrings.OTHERBIOMARKERS)) {
                    requestQuery.setHugoSymbol(symbol);
                } else {
                    requestQuery.setHugoSymbol(symbol.toUpperCase());
                }
                queries.add(requestQuery);
            }
        }

        if (evidenceType != null) {
            for (String type : evidenceType.trim().split("\\s*,\\s*")) {
                try {
                    EvidenceType et = EvidenceType.valueOf(type);
                    evidenceTypes.add(et);
                } catch (Exception e) {
                    // nothing needs to be done
                }
            }
        } else {
            evidenceTypes = EvidenceTypeUtils.getAllEvidenceTypes();
        }

        if (alteration != null) {
            String[] alts = alteration.trim().split("\\s*,\\s*");
            if (queries.size() == alts.length) {
                String[] consequences = consequence == null ? new String[0] : consequence.trim().split("\\s*,\\s*");
                String[] proteinStarts = proteinStart == null ? new String[0] : proteinStart.trim().split("\\s*,\\s*");
                String[] proteinEnds = proteinEnd == null ? new String[0] : proteinEnd.trim().split("\\s*,\\s*");

                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setAlteration(alts[i]);
                    queries.get(i).setConsequence(consequences.length == alts.length ? consequences[i] : null);
                    queries.get(i).setProteinStart(proteinStarts.length == alts.length ? Integer.valueOf(proteinStarts[i]) : null);
                    queries.get(i).setProteinEnd(proteinEnds.length == alts.length ? Integer.valueOf(proteinEnds[i]) : null);
                }
            } else {
                return null;
            }
        }

        String[] tumorTypes = tumorType == null ? new String[0] : tumorType.trim().split("\\s*,\\s*");
        if (tumorTypes.length > 0) {
            if (tumorTypes.length == 1) {
                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setTumorType(tumorTypes[0]);
                }
            } else if (queries.size() == tumorTypes.length) {
                for (int i = 0; i < queries.size(); i++) {
                    queries.get(i).setTumorType(tumorTypes[i]);
                }
            }
        }

        if (levels != null) {
            String[] levelStrs = levels.trim().split("\\s*,\\s*");
            for (int i = 0; i < levelStrs.length; i++) {
                LevelOfEvidence level = LevelOfEvidence.getByName(levelStrs[i]);
                if (level != null) {
                    levelOfEvidences.add(level);
                }
            }
        } else {
            levelOfEvidences = null;
        }

        requestQueries.put("queries", queries);
        requestQueries.put("evidenceTypes", evidenceTypes);
        requestQueries.put("levels", levelOfEvidences);
        return requestQueries;
    }

    public static Long printTimeDiff(Long oldDate, Long newDate, String message) {
        System.out.println(message + ": " + (newDate - oldDate));
        return newDate;
    }

    public static MutationEffect findHighestMutationEffect(Set<MutationEffect> mutationEffect) {
        Integer index = 100;
        for (MutationEffect effect : mutationEffect) {
            if (PRIORITIZED_MUTATION_EFFECTS.indexOf(effect) < index) {
                index = PRIORITIZED_MUTATION_EFFECTS.indexOf(effect);
            }
        }
        return index == 100 ? null : PRIORITIZED_MUTATION_EFFECTS.get(index);
    }

    public static IndicatorQueryMutationEffect findHighestMutationEffectByEvidence(Set<Evidence> evidences) {
        int index = 100;
        IndicatorQueryMutationEffect indicatorQueryMutationEffect = new IndicatorQueryMutationEffect();
        for (Evidence evidence : evidences) {
            MutationEffect mutationEffect = MutationEffect.getByName(evidence.getKnownEffect());
            int _index = PRIORITIZED_MUTATION_EFFECTS.indexOf(mutationEffect);
            if (_index < index) {
                indicatorQueryMutationEffect.setMutationEffect(mutationEffect);
                indicatorQueryMutationEffect.setMutationEffectEvidence(evidence);
                index = _index;
            }
        }
        return indicatorQueryMutationEffect;
    }

    public static IndicatorQueryMutationEffect setToAlternativeAlleleMutationEffect(IndicatorQueryMutationEffect indicatorQueryMutationEffect) {
        if (indicatorQueryMutationEffect != null && indicatorQueryMutationEffect.getMutationEffect() != null) {
            MutationEffect mutationEffect = indicatorQueryMutationEffect.getMutationEffect();
            MutationEffect likeME = MutationEffect.getByName("Likely " + mutationEffect.getMutationEffect().replaceAll("(?i)likely", "").trim());

            // likeME will be null if mutation effect without related likely mutation effect.
            if (likeME == null || likeME.equals(MutationEffect.LIKELY_NEUTRAL))
                return new IndicatorQueryMutationEffect();
            indicatorQueryMutationEffect.setMutationEffect(likeME);
        }
        return indicatorQueryMutationEffect;
    }

    public static Oncogenicity findHighestOncogenicity(Set<Oncogenicity> oncogenicitySet) {
        Integer index = -1;

        for (Oncogenicity datum : oncogenicitySet) {
            if (datum != null) {
                Integer oncogenicIndex = PRIORITIZED_ONCOGENICITY.indexOf(datum);
                if (index < oncogenicIndex) {
                    index = oncogenicIndex;
                }
            }
        }
        return index == -1 ? null : PRIORITIZED_ONCOGENICITY.get(index);
    }

    public static Integer compareOncogenicity(Oncogenicity o1, Oncogenicity o2, Boolean asc) {
        if (asc == null) {
            asc = true;
        }
        if (o1 == null) {
            if (o2 == null)
                return 0;
            return asc ? 1 : -1;
        }
        if (o2 == null)
            return asc ? -1 : 1;
        return (PRIORITIZED_ONCOGENICITY.indexOf(o2) - PRIORITIZED_ONCOGENICITY.indexOf(o1)) * (asc ? 1 : -1);
    }

    public static Oncogenicity findHighestOncogenicByEvidences(Set<Evidence> evidences) {
        Set<Oncogenicity> oncogenicitySet = new HashSet<>();

        if (evidences != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getKnownEffect() != null) {
                    oncogenicitySet.add(Oncogenicity.getByEffect(evidence.getKnownEffect()));
                }
            }
        }

        return findHighestOncogenicity(oncogenicitySet);
    }

    public static Evidence findHighestOncogenicEvidenceByEvidences(Set<Evidence> evidences) {
        Oncogenicity oncogenicity = findHighestOncogenicByEvidences(evidences);
        Evidence evidencePicked = null;

        if (oncogenicity != null) {
            for (Evidence evidence : evidences) {
                if (evidence.getKnownEffect().equals(oncogenicity.getOncogenic())) {
                    if (evidencePicked == null) {
                        evidencePicked = evidence;
                    } else if (evidencePicked.getLastEdit() == null) {
                        evidencePicked = evidence;
                    } else if (evidence.getLastEdit() != null && evidence.getLastEdit().after(evidencePicked.getLastEdit())) {
                        evidencePicked = evidence;
                    }
                }
            }
        }
        return evidencePicked;
    }

    public static Oncogenicity setToAlleleOncogenicity(Oncogenicity oncogenicity) {
        if (oncogenicity == null) {
            return null;
        }
        if (oncogenicity.equals(Oncogenicity.YES) || oncogenicity.equals(Oncogenicity.LIKELY)) {
            return Oncogenicity.LIKELY;
        } else {
            return null;
        }
    }

    public static String getAlleleConflictsMutationEffect(Set<String> mutationEffects) {
        Set<String> clean = new HashSet<>();

        for (String mutationEffect : mutationEffects) {
            if (mutationEffect != null) {
                mutationEffect = mutationEffect.replaceAll("(?i)likely", "");
                mutationEffect = mutationEffect.replaceAll("\\s", "");
                clean.add(mutationEffect);
            }
        }

        if (clean.size() > 1) {
            return "Unknown";
        } else if (clean.size() == 1) {
            return "Likely " + clean.iterator().next();
        } else {
            return "";
        }
    }


    public static Long getCurrentTimestamp() {
        return new Date().getTime();
    }

    public static Long getTimestampDiff(Long old) {
        return new Date().getTime() - old;
    }

    public static String getDataVersion() {
        if (DataVersion == null) {
            DataVersion = getProperty("data.version");
        }
        return DataVersion;
    }

    public static String getDataVersionDate() {
        if (DataVersionDate == null) {
            DataVersionDate = getProperty("data.version_date");
        }
        return DataVersionDate;
    }

    private static String getProperty(String propertyName) {
        String version = "";
        if (propertyName != null) {
            try {
                String tmpData = PropertiesUtils.getProperties(propertyName);
                if (tmpData != null) {
                    version = tmpData;
                }
            } catch (Exception e) {
            }
        }
        return version;
    }

    public static String listToString(List<String> list, String separator) {
        return listToString(list, separator, false);
    }

    public static String listToString(List<String> list, String separator, boolean sort) {
        if (list.isEmpty()) {
            return "";
        }

        if (sort) {
            Collections.sort(list);
        }

        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        if (n == 1) {
            return sb.toString();
        }

        for (int i = 1; i < n; i++) {
            sb.append(separator).append(list.get(i));
        }

        return sb.toString();
    }

    public static String listToString(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }

        int n = list.size();
        StringBuilder sb = new StringBuilder();
        sb.append(list.get(0));
        if (n == 1) {
            return sb.toString();
        } else if (n == 2) {
            return sb.append(" and ").append(list.get(1)).toString();
        }

        for (int i = 1; i < n - 1; i++) {
            sb.append(", ").append(list.get(i));
        }
        sb.append(" and ").append(list.get(n - 1));

        return sb.toString();
    }

    public static List<Integer> stringToIntegers(String ids) {
        if (ids == null) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (String id : ids.trim().split("\\s*,\\s*")) {
            Integer match = Integer.parseInt(id);

            if (match != null) {
                result.add(match);
            }
        }
        return result;
    }

    public static List<EvidenceType> stringToEvidenceTypes(String string, String separator) {
        List<EvidenceType> evidenceTypes = new ArrayList<>();
        if (string != null) {
            if (separator == null) {
                separator = ",";
            }
            for (String type : string.trim().split("\\s*" + separator + "\\s*")) {
                EvidenceType et = EvidenceType.valueOf(type);
                evidenceTypes.add(et);
            }
        }
        return evidenceTypes;
    }

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration,
                                                   String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public static boolean isValidHotspotOncogenicity(Oncogenicity oncogenicity) {
        if (oncogenicity == null)
            return false;
        Set<Oncogenicity> oncogenicities = new HashSet<>();
        oncogenicities.add(Oncogenicity.YES);
        oncogenicities.add(Oncogenicity.LIKELY);
        oncogenicities.add(Oncogenicity.PREDICTED);
        oncogenicities.add(Oncogenicity.LIKELY_NEUTRAL);
        oncogenicities.add(Oncogenicity.INCONCLUSIVE);

        return oncogenicities.contains(oncogenicity);
    }

    public static Set<BiologicalVariant> getBiologicalVariants(Gene gene) {
        Set<BiologicalVariant> variants = new HashSet<>();
        if (gene != null) {
            Long oldTime = new Date().getTime();
            List<Alteration> alterations;

            alterations = AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(null, gene)));
            alterations = AlterationUtils.excludeInferredAlterations(alterations);
            alterations = AlterationUtils.excludePositionedAlterations(alterations);

//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all alterations for " + hugoSymbol);

            Set<EvidenceType> evidenceTypes = new HashSet<EvidenceType>() {{
                add(EvidenceType.MUTATION_EFFECT);
                add(EvidenceType.ONCOGENIC);
            }};
            Map<Alteration, Map<EvidenceType, Set<Evidence>>> evidences = new HashMap<>();

            for (Alteration alteration : alterations) {
                Map<EvidenceType, Set<Evidence>> map = new HashMap<>();
                map.put(EvidenceType.ONCOGENIC, new HashSet<Evidence>());
                map.put(EvidenceType.MUTATION_EFFECT, new HashSet<Evidence>());
                evidences.put(alteration, map);
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Initialize evidences.");

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Get all gene evidences.");

            for (Evidence evidence : geneEvidences.get(gene)) {
                for (Alteration alteration : evidence.getAlterations()) {
                    if (evidences.containsKey(alteration)) {
                        evidences.get(alteration).get(evidence.getEvidenceType()).add(evidence);
                    }
                }
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Seperate evidences.");

            for (Map.Entry<Alteration, Map<EvidenceType, Set<Evidence>>> entry : evidences.entrySet()) {
                Alteration alteration = entry.getKey();
                Map<EvidenceType, Set<Evidence>> map = entry.getValue();

                BiologicalVariant variant = new BiologicalVariant();
                variant.setVariant(alteration);
                Oncogenicity oncogenicity = EvidenceUtils.getOncogenicityFromEvidence(map.get(EvidenceType.ONCOGENIC));
                MutationEffect mutationEffect = EvidenceUtils.getMutationEffectFromEvidence(map.get(EvidenceType.MUTATION_EFFECT));
                variant.setOncogenic(oncogenicity == null ? null : oncogenicity.getOncogenic());
                variant.setMutationEffect(mutationEffect == null ? null : mutationEffect.getMutationEffect());
                variant.setOncogenicPmids(EvidenceUtils.getPmids(map.get(EvidenceType.ONCOGENIC)));
                variant.setMutationEffectPmids(EvidenceUtils.getPmids(map.get(EvidenceType.MUTATION_EFFECT)));
                variant.setOncogenicAbstracts(EvidenceUtils.getAbstracts(map.get(EvidenceType.ONCOGENIC)));
                variant.setMutationEffectAbstracts(EvidenceUtils.getAbstracts(map.get(EvidenceType.MUTATION_EFFECT)));
                variants.add(variant);
            }
//                oldTime = MainUtils.printTimeDiff(oldTime, new Date().getTime(), "Created biological annotations.");
        }
        return variants;
    }

    public static Set<ClinicalVariant> getClinicalVariants(Gene gene) {
        Set<ClinicalVariant> variants = new HashSet<>();
        if (gene != null) {
            List<Alteration> alterations;
            alterations = AlterationUtils.excludeVUS(gene, new ArrayList<>(AlterationUtils.getAllAlterations(null, gene)));
            Set<EvidenceType> evidenceTypes = EvidenceTypeUtils.getTreatmentEvidenceTypes();
            Map<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> evidences = new HashMap<>();
            Set<LevelOfEvidence> publicLevels = LevelUtils.getPublicLevels();

            for (Alteration alteration : alterations) {
                evidences.put(alteration, new HashMap<TumorType, Map<LevelOfEvidence, Set<Evidence>>>());
            }

            Map<Gene, Set<Evidence>> geneEvidences =
                EvidenceUtils.getEvidenceByGenesAndEvidenceTypes(Collections.singleton(gene), evidenceTypes);

            for (Evidence evidence : geneEvidences.get(gene)) {
                TumorType oncoTreeType = evidence.getOncoTreeType();

                if (oncoTreeType != null) {
                    for (Alteration alteration : evidence.getAlterations()) {
                        if (evidences.containsKey(alteration)) {
                            if (!evidences.get(alteration).containsKey(oncoTreeType)) {
                                evidences.get(alteration).put(oncoTreeType, new HashMap<LevelOfEvidence, Set<Evidence>>());
                            }
                            if (publicLevels.contains(evidence.getLevelOfEvidence())) {
                                LevelOfEvidence levelOfEvidence = evidence.getLevelOfEvidence();
                                if (!evidences.get(alteration).get(oncoTreeType).containsKey(levelOfEvidence)) {
                                    evidences.get(alteration).get(oncoTreeType).put(levelOfEvidence, new HashSet<Evidence>());
                                }
                                evidences.get(alteration).get(oncoTreeType).get(levelOfEvidence).add(evidence);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Alteration, Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>>> entry : evidences.entrySet()) {
                Alteration alteration = entry.getKey();
                IndicatorQueryOncogenicity oncogenicity = IndicatorUtils.getOncogenicity(alteration, AlterationUtils.getAlleleAlterations(null, alteration), AlterationUtils.getRelevantAlterations(null, alteration));
                String oncogenicityString = null;
                if (oncogenicity.getOncogenicity() != null) {
                    oncogenicityString = oncogenicity.getOncogenicity().getOncogenic();
                }
                Map<TumorType, Map<LevelOfEvidence, Set<Evidence>>> map = entry.getValue();

                for (Map.Entry<TumorType, Map<LevelOfEvidence, Set<Evidence>>> _entry : map.entrySet()) {
                    TumorType oncoTreeType = _entry.getKey();

                    for (Map.Entry<LevelOfEvidence, Set<Evidence>> __entry : _entry.getValue().entrySet()) {
                        ClinicalVariant variant = new ClinicalVariant();
                        variant.setOncoTreeType(oncoTreeType);
                        variant.setVariant(alteration);
                        variant.setOncogenic(oncogenicityString);
                        variant.setLevel(__entry.getKey().getLevel());
                        variant.setDrug(EvidenceUtils.getDrugs(__entry.getValue()));
                        variant.setDrugPmids(EvidenceUtils.getPmids(__entry.getValue()));
                        variant.setDrugAbstracts(EvidenceUtils.getAbstracts(__entry.getValue()));
                        variants.add(variant);
                    }
                }
            }
        }
        return variants;
    }

    public static boolean containsCaseInsensitive(String s, Set<String> l) {
        for (String string : l) {
            if (string.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentTime() {
        return getCurrentTime(null);
    }

    public static String getCurrentTime(String timeFormat) {
        if (timeFormat == null) {
            timeFormat = "MM/dd/yyy hh:mm:ss";
        }
        return new SimpleDateFormat(timeFormat).format(new Date());
    }

    public static String getTimeByDate(Date date) {
        return getTimeByDate(date, null);
    }

    public static String getTimeByDate(Date date, String timeFormat) {
        if (date == null)
            return null;
        if (timeFormat == null) {
            timeFormat = "MM/dd/yyy hh:mm:ss";
        }
        return new SimpleDateFormat(timeFormat).format(date);
    }

    public static Boolean isVUS(Alteration alteration) {
        List<Evidence> evidenceList = EvidenceUtils.getEvidence(Collections.singletonList(alteration), Collections.singleton(EvidenceType.VUS), null);
        return !(evidenceList == null || evidenceList.isEmpty());
    }

    public static Map<String, Boolean> validateTrials(List<String> nctIds) throws ParserConfigurationException, SAXException, IOException {
        Map<String, Boolean> result = new HashMap<>();
        if (nctIds == null || nctIds.size() == 0) {
            return result;
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        for (String nctId : nctIds) {
            String strUrl = "https://clinicaltrials.gov/show/" + nctId + "?displayxml=true";
            try {
                Document doc = db.parse(strUrl);
                result.put(nctId, true);
            } catch (IOException e) {
                result.put(nctId, false);
            }
        }
        return result;
    }

    public static Citations getCitationsByEvidence(Evidence evidence) {
        Citations citations = new Citations();
        for (Article article : evidence.getArticles()) {
            if (article.getPmid() != null) {
                citations.getPmids().add(article.getPmid());
            }
            if (article.getAbstractContent() != null) {
                ArticleAbstract articleAbstract = new ArticleAbstract();
                articleAbstract.setAbstractContent(article.getAbstractContent());
                articleAbstract.setLink(article.getLink());
                citations.getAbstracts().add(articleAbstract);
            }
        }
        return citations;
    }

    public static void sortAnnotatedVariants(List<AnnotatedVariant> variants) {
        Collections.sort(variants, new Comparator<AnnotatedVariant>() {
            @Override
            public int compare(AnnotatedVariant a1, AnnotatedVariant a2) {
                // Gene
                int result = a1.getGene().compareTo(a2.getGene());

                // Alteration
                if (result == 0) {
                    result = a1.getVariant().compareTo(a2.getVariant());

                    // Oncogenicity
                    if (result == 0) {
                        result = MainUtils.compareOncogenicity(
                            Oncogenicity.getByEffect(a1.getOncogenicity()),
                            Oncogenicity.getByEffect(a2.getOncogenicity()),
                            true
                        );

                        // Mutation Effect
                        if (result == 0) {
                            result = a1.getMutationEffect() == null ? 1 : a1.getMutationEffect().compareTo(a2.getMutationEffect());
                        }
                    }
                }
                return result;
            }
        });
    }

    public static void sortActionableVariants(List<ActionableGene> variants) {
        Collections.sort(variants, new Comparator<ActionableGene>() {
            @Override
            public int compare(ActionableGene a1, ActionableGene a2) {
                // Level
                int result = LevelUtils.compareLevel(
                    LevelOfEvidence.getByLevel(a1.getLevel()),
                    LevelOfEvidence.getByLevel(a2.getLevel())
                );
                // Gene
                if (result == 0) {
                    result = a1.getGene().compareTo(a2.getGene());

                    // Cancer Type
                    if (result == 0) {
                        result = a1.getCancerType().compareTo(a2.getCancerType());

                        // Alteration
                        if (result == 0) {
                            result = a1.getVariant().compareTo(a2.getVariant());
                        }
                    }
                }
                return result;
            }
        });
    }

    public static void sortCuratedGenes(List<CuratedGene> genes){
        Collections.sort(genes, new Comparator<CuratedGene>() {
            @Override
            public int compare(CuratedGene g1, CuratedGene g2) {
                return g1.getHugoSymbol().compareTo(g2.getHugoSymbol());
            }
        });
    }
}
