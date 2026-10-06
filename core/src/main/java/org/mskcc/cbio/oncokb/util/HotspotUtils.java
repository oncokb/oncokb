package org.mskcc.cbio.oncokb.util;

import org.genome_nexus.client.Hotspot;
import org.genome_nexus.client.IntegerRange;
import org.genome_nexus.client.ProteinLocation;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationPositionBoundary;
import org.mskcc.cbio.oncokb.model.CancerHotspot;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;
import static org.mskcc.cbio.oncokb.Constants.SPLICE_SITE_VARIANTS;
import static org.mskcc.cbio.oncokb.util.HotspotUtils.extractProteinPos;
import static org.mskcc.cbio.oncokb.util.VariantConsequenceUtils.toGNMutationType;

/**
 * Created by Hongxin on 11/03/16.
 */

class EnrichedHotspot extends Hotspot {
    Integer start;
    Integer end;
    Integer tumorCount;
    Set<String> pmids = new LinkedHashSet<>();

    public EnrichedHotspot(Hotspot hotspot) {
        this.setHugoSymbol(hotspot.getHugoSymbol());
        this.setType(hotspot.getType());
        this.setResidue(hotspot.getResidue());

        // Protein location
        IntegerRange integerRange = extractProteinPos(this.getResidue());
        this.setStart(integerRange.getStart());
        this.setEnd(integerRange.getEnd());
    }


    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getTumorCount() {
        return tumorCount;
    }

    public void setTumorCount(Integer tumorCount) {
        this.tumorCount = tumorCount;
    }

    public Set<String> getPmids() {
        return pmids;
    }

    public void setPmids(Set<String> pmids) {
        this.pmids = pmids;
    }
}

public class HotspotUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotspotUtils.class);
    private static final String HOTSPOT_FILE_PATH = "/data/hotspots_v2_and_3d.txt";
    private static final String DELIMITER = "\t";
    private static final String COMMENT_PREFIX = "#";
    private static Map<Gene, List<EnrichedHotspot>> hotspotMutations = new HashMap<>();
    private static final String POSITIONAL_MUTATION_TYPE="positional";
    private static final String RANGE_INFRAME_MUTATION_TYPE="rangeInframe";
    public static final String SINGLE_RESIDUE_HOTSPOT_TYPE = "single residue";
    public static final String IN_FRAME_INDEL_HOTSPOT_TYPE = "in-frame indel";
    public static final String SPLICE_SITE_HOTSPOT_TYPE = "splice site";
    public static final List<String> HOTSPOT_METHOD_PMIDS = Collections.unmodifiableList(Arrays.asList("26619011", "29247016", "41895280"));

    static {
        LOGGER.info("Cache all hotspots");
        getHotspotsFromDataFile();
    }

    private static void getHotspotsFromDataFile() {
        List<EnrichedHotspot> hotspots = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(HotspotUtils.class.getResourceAsStream(HOTSPOT_FILE_PATH)))) {
            Map<String, Integer> columnIndex = new HashMap<>();
            String[] header = readNextDataLine(reader).split(DELIMITER, -1);
            for (int i = 0; i < header.length; i++) {
                columnIndex.put(header[i], i);
            }

            String line;
            while ((line = readNextDataLine(reader)) != null) {
                String[] parts = line.split(DELIMITER, -1);
                Hotspot hotspot = new Hotspot();
                hotspot.setHugoSymbol(parts[columnIndex.get("hugo_symbol")]);
                hotspot.setType(parts[columnIndex.get("type")]);
                hotspot.setResidue(parts[columnIndex.get("residue")]);
                EnrichedHotspot enrichedHotspot = new EnrichedHotspot(hotspot);
                enrichedHotspot.setPmids(parsePmids(parts[columnIndex.get("pmids")]));
                enrichedHotspot.setTumorCount(parseTumorCount(parts[columnIndex.get("tumor_count")]));
                hotspots.add(enrichedHotspot);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read the hotspot data file " + HOTSPOT_FILE_PATH, e);
        }
        parseData(hotspots);
    }

    private static Set<String> parsePmids(String value) {
        Set<String> pmids = new LinkedHashSet<>();
        for (String pmid : value.split(",")) {
            if (!pmid.trim().isEmpty()) {
                pmids.add(pmid.trim());
            }
        }
        return pmids;
    }

    private static Integer parseTumorCount(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return (int) Double.parseDouble(value.trim());
    }

    // Skips the leading source comment and any blank lines
    private static String readNextDataLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty() && !line.startsWith(COMMENT_PREFIX)) {
                return line;
            }
        }
        return null;
    }

    private static void parseData(List<EnrichedHotspot> hotspots) {
        if (hotspots != null) {
            for (EnrichedHotspot hotspotMutation : hotspots) {
                Gene gene = GeneUtils.getGeneByHugoSymbol(hotspotMutation.getHugoSymbol());
                if (gene != null) {
                    if (!hotspotMutations.containsKey(gene)) {
                        hotspotMutations.put(gene, new ArrayList<EnrichedHotspot>());
                    }
                    hotspotMutations.get(gene).add(hotspotMutation);
                }
            }
        }
    }

    public static CancerHotspot getHotspot(Gene gene, String residue) {
        if (gene == null || residue == null || hotspotMutations.get(gene) == null) {
            return null;
        }
        for (EnrichedHotspot hotspot : hotspotMutations.get(gene)) {
            if (!hotspot.getType().equals("3d") && hotspot.getResidue().equalsIgnoreCase(residue.trim())) {
                return toCancerHotspot(gene, hotspot);
            }
        }
        return null;
    }

    public static List<Alteration> getCuratedAlterations(Gene gene, CancerHotspot hotspot) {
        if (gene == null || hotspot == null) {
            return new ArrayList<>();
        }
        List<Alteration> candidates = new ArrayList<>();
        for (Alteration alteration : AlterationUtils.getAllAlterations(null, gene)) {
            if (alteration.getConsequence() == null || alteration.getProteinStart() == null || alteration.getProteinEnd() == null
                || alteration.getAlteration().equalsIgnoreCase(hotspot.getResidue())) {
                continue;
            }
            if (isCuratedAlterationOnHotspot(alteration, hotspot)) {
                candidates.add(alteration);
            }
        }
        List<Alteration> curated = AlterationUtils.excludeVUS(candidates);
        curated.sort(Comparator.comparing(Alteration::getProteinStart).thenComparing(Alteration::getProteinEnd).thenComparing(Alteration::getAlteration));
        return curated;
    }

    private static boolean isCuratedAlterationOnHotspot(Alteration alteration, CancerHotspot hotspot) {
        int start = alteration.getProteinStart();
        int end = alteration.getProteinEnd();
        if (IN_FRAME_INDEL_HOTSPOT_TYPE.equals(hotspot.getType())) {
            return AlterationUtils.isInframeAlteration(alteration) && start <= hotspot.getProteinEnd() && end >= hotspot.getProteinStart();
        }
        if (SPLICE_SITE_HOTSPOT_TYPE.equals(hotspot.getType())) {
            return SPLICE_SITE_VARIANTS.contains(alteration.getConsequence()) && start <= hotspot.getProteinEnd() && end >= hotspot.getProteinStart();
        }
        return alteration.getConsequence().getTerm().equals(MISSENSE_VARIANT)
            && start == end
            && start == hotspot.getProteinStart()
            && (alteration.getRefResidues() + start).equalsIgnoreCase(hotspot.getResidue());
    }

    public static String getHotspotDisplayName(String residue, String type) {
        if (IN_FRAME_INDEL_HOTSPOT_TYPE.equals(type)) {
            return residue.replace("-", "_") + "insdel";
        }
        return residue;
    }

    private static CancerHotspot toCancerHotspot(Gene gene, EnrichedHotspot hotspot) {
        CancerHotspot cancerHotspot = new CancerHotspot();
        cancerHotspot.setHugoSymbol(gene.getHugoSymbol());
        cancerHotspot.setResidue(hotspot.getResidue());
        cancerHotspot.setName(getHotspotDisplayName(hotspot.getResidue(), hotspot.getType()));
        cancerHotspot.setType(hotspot.getType());
        cancerHotspot.setProteinStart(hotspot.getStart());
        cancerHotspot.setProteinEnd(hotspot.getEnd());
        cancerHotspot.setTumorCount(hotspot.getTumorCount());
        cancerHotspot.setPmids(new LinkedHashSet<>(hotspot.getPmids()));
        return cancerHotspot;
    }

    public static boolean isHotspot(Alteration alteration) {
        return getHotspotType(alteration) != null;
    }

    /**
     * The type of the hotspots the alteration matches — "single residue",
     * "in-frame indel" or "splice site" — or null when it is not a hotspot. A
     * caller that knows which hotspot it is looking at needs this to tell, say,
     * a single residue hotspot mutation from an in-frame indel that merely
     * covers the same position.
     *
     * There is only ever one type to report: the alteration's own mutation type
     * decides which kind of hotspot it can be paired with, so the matches below
     * all share a type even when the alteration covers several hotspots.
     */
    public static String getHotspotType(Alteration alteration) {
        for (EnrichedHotspot hotspot : getMatchedHotspots(alteration)) {
            return hotspot.getType();
        }
        return null;
    }

    public static Set<String> getHotspotPmids(Alteration alteration) {
        Set<String> pmids = new LinkedHashSet<>();
        for (EnrichedHotspot hotspot : getMatchedHotspots(alteration)) {
            pmids.addAll(hotspot.getPmids());
        }
        return pmids;
    }

    private static List<EnrichedHotspot> getMatchedHotspots(Alteration alteration) {
        if (alteration == null || alteration.getGene() == null || alteration.getProteinStart().intValue() == AlterationPositionBoundary.START.getValue() || alteration.getProteinEnd().intValue() == AlterationPositionBoundary.END.getValue()) {
            return Collections.emptyList();
        }

        // There are few genes we cannot map to GRCh38 yet
        Set<String> notMappedHugos = new HashSet<>();
        notMappedHugos.add("MYD88");
        notMappedHugos.add("TET3");
        notMappedHugos.add("RYBP");
        notMappedHugos.add("WT1");
        if (notMappedHugos.contains(alteration.getGene().getHugoSymbol()) && !alteration.getReferenceGenomes().contains(ReferenceGenome.GRCh37)) {
            return Collections.emptyList();
        }

        AlterationUtils.annotateAlteration(alteration, alteration.getAlteration());

        ProteinLocation proteinLocation = new ProteinLocation();
        proteinLocation.setStart(alteration.getProteinStart());
        proteinLocation.setEnd(alteration.getProteinEnd());
        String mutationType = toGNMutationType(alteration.getConsequence());
        if (AlterationUtils.isPositionedAlteration(alteration)) {
            mutationType = POSITIONAL_MUTATION_TYPE;
        } else if (AlterationUtils.isRangeInframeAlteration(alteration)) {
            mutationType = RANGE_INFRAME_MUTATION_TYPE;
        }
        proteinLocation.setMutationType(mutationType);
        List<EnrichedHotspot> hotspots = new ArrayList<>();

        if (hotspotMutations.get(alteration.getGene()) == null) {
            return Collections.emptyList();
        }

        // for alteration that is missense but ends as mis, it is a range mutation
        if(alteration.getConsequence() != null && alteration.getConsequence().equals(VariantConsequenceUtils.findVariantConsequenceByTerm(MISSENSE_VARIANT)) && alteration.getAlteration().endsWith("mis")) {
            return Collections.emptyList();
        }

        for (EnrichedHotspot hotspot : hotspotMutations.get(alteration.getGene())) {
            if (!hotspot.getType().equals("3d")) {
                hotspots.add(hotspot);
            }
        }
        return proteinLocationHotspotsFilter(hotspots, proteinLocation, alteration.getRefResidues());
    }

    // Logic from GN
    private static List<EnrichedHotspot> proteinLocationHotspotsFilter(List<EnrichedHotspot> hotspots, ProteinLocation proteinLocation, String referenceResidues) {
        int start = proteinLocation.getStart();
        int end = proteinLocation.getEnd();
        String type = proteinLocation.getMutationType();
        List<EnrichedHotspot> result = new ArrayList<>();

        for (EnrichedHotspot hotspot : hotspots) {
            boolean validPosition = true;

            // Protein location
            int hotspotStart = hotspot.getStart();
            int hotspotStop = hotspot.getEnd();
            if (type.equals(RANGE_INFRAME_MUTATION_TYPE)) {
                validPosition = (start >= hotspotStart && end <= hotspotStop);
            } else {
                validPosition = (start <= hotspotStop && end >= hotspotStart);
            }

            // Mutation type
            boolean validPositional = type.equals(POSITIONAL_MUTATION_TYPE) && (hotspot.getType().contains("3d") || hotspot.getType().contains("single residue"));
            boolean validMissense = type.equals("Missense_Mutation") && (hotspot.getType().contains("3d") || hotspot.getType().contains("single residue"));
            boolean validInFrameRange = type.equals(RANGE_INFRAME_MUTATION_TYPE) && (hotspot.getType().contains("in-frame"));
            boolean validInFrameInsertion = type.equals("In_Frame_Ins") && (hotspot.getType().contains("in-frame"));
            boolean validInFrameDeletion = type.equals("In_Frame_Del") && (hotspot.getType().contains("in-frame"));
            boolean validSplice = (type.equals("Splice_Site") || type.equals("Splice_Region")) && (hotspot.getType().contains("splice"));

            // Add hotspot
            if (validPosition && (validPositional || validMissense || validInFrameRange || validInFrameInsertion || validInFrameDeletion || validSplice)) {
                if(validPositional || validMissense) {
                    boolean validReferenceResidues = (referenceResidues + proteinLocation.getStart()).equalsIgnoreCase(hotspot.getResidue());
                    if (validReferenceResidues) {
                        result.add(hotspot);
                    }
                } else {
                    result.add(hotspot);
                }
            }
        }

        return result;
    }

    public static IntegerRange extractProteinPos(String proteinChange) {
        IntegerRange proteinPos = null;
        Integer start = -1;
        Integer end = -1;

        List<Integer> positions = extractPositiveIntegers(proteinChange);

        // ideally positions.size() should always be 2
        if (positions.size() >= 2) {
            start = positions.get(0);
            end = positions.get(positions.size() - 1);
        }
        // in case no end point, use start as end
        else if (positions.size() > 0) {
            start = end = positions.get(0);
        }

        if (!start.equals(-1)) {
            proteinPos = new IntegerRange();
            proteinPos.setStart(start);
            proteinPos.setEnd(end);
        }

        return proteinPos;
    }

    private static List<Integer> extractPositiveIntegers(String input) {
        if (input == null) {
            return Collections.emptyList();
        }

        List<Integer> list = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(input);

        while (m.find()) {
            list.add(Integer.parseInt(m.group()));
        }

        return list;
    }
}
