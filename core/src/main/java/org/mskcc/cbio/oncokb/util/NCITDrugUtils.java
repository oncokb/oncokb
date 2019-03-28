package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.NCITDrug;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin Zhang on 10/18/18.
 */
public class NCITDrugUtils {
    private static boolean allNcitDrugsInitialized = false;
    private static Set<NCITDrug> allNcitDrugs = new HashSet<>();

    public static NCITDrug findDrugByNcitCode(String ncitCode) {
        if (!allNcitDrugsInitialized) {
            cacheDrugs();
            allNcitDrugsInitialized = true;
        }

        for (NCITDrug drug : allNcitDrugs) {
            if (drug.getNcitCode().equals(ncitCode)) {
                return drug;
            }
        }
        return null;
    }

    public static LinkedHashSet<NCITDrug> findDrugs(String query) {
        LinkedHashSet<NCITDrug> matches = new LinkedHashSet<>();
        if (!allNcitDrugsInitialized) {
            cacheDrugs();
            allNcitDrugsInitialized = true;
        }
        return findMatches(query);
    }

    private static LinkedHashSet<NCITDrug> findMatches(String query) {
        TreeSet<NCITDrug> matches = new TreeSet<>(new NCTIDrugComp(query));

        if (query == null) {
            return new LinkedHashSet<>();
        }
        for (NCITDrug drug : allNcitDrugs) {
            if (drug.getNcitCode().contains(query)) {
                matches.add(drug);
                continue;
            }
            if (drug.getDrugName().contains(query)) {
                matches.add(drug);
                continue;
            }

            boolean found = false;
            for (String synonym : drug.getSynonyms()) {
                if (synonym.contains(query)) {
                    matches.add(drug);
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
        }
        return new LinkedHashSet<>(matches);
    }


    private static void cacheDrugs() {
        System.out.println("getting accepted semantic types...");
        Set<String> acceptedSemanticTypes = getAcceptedSemanticTypes();

        List<String> lines = null;
        try {
            lines = FileUtils.readTrimedLinesStream(
                NCITDrugUtils.class.getResourceAsStream("/data/Thesaurus.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            lines = new ArrayList<>();
        }
        int nLines = lines.size();

        // code <tab> concept name <tab> parents <tab> synonyms <tab> definition <tab> display name <tab> concept status <tab> semantic type <EOL>

        for (int i = 0; i < nLines; i++) {
            if ((i + 1) % 1000 == 0) {
                System.out.println("Cached " + (i + 1));
            }
            String line = lines.get(i);
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\t");

            if (parts.length != 8) {
                continue;
            }
            String code = parts[0] == null ? null : parts[0].trim();
            String synonyms = parts[3] == null ? null : parts[3].trim();
            String definition = parts[4] == null ? null : parts[4].trim();
            String conceptStatus = parts[6] == null ? null : parts[6].trim();
            String semanticType = parts[7] == null ? null : parts[7].trim();
            List<String> synonymsList = new ArrayList<>();

            if (StringUtils.isNullOrEmpty(code)) {
                continue;
            } else if (!code.startsWith("C")) {
                continue;
            }

            if (StringUtils.isNullOrEmpty(semanticType)) {
                continue;
            } else {
                Set<String> semanticTypes = new HashSet(Arrays.asList((semanticType.split("\\|"))));
                boolean disjoint = Collections.disjoint(semanticTypes, acceptedSemanticTypes);
                if (disjoint) {
                    continue;
                }
            }

            if (!StringUtils.isNullOrEmpty(conceptStatus)) {
                continue;
            }

            if (StringUtils.isNullOrEmpty(synonyms)) {
                continue;
            } else {
                // Arrays.asList returning a fixed-size list, you cannot remove item from the list, need to reinitialize a new array list
                synonymsList = new ArrayList<>(Arrays.asList((synonyms.split("\\|"))));
            }

            NCITDrug drug = new NCITDrug();
            drug.setNcitCode(code);
            if (!synonymsList.isEmpty()) {
                drug.setDrugName(synonymsList.get(0));
                synonymsList.remove(0);
                drug.setSynonyms(new HashSet<>(synonymsList));
                if (!StringUtils.isNullOrEmpty(definition)) {
                    drug.setDescription(definition);
                }
            }
            allNcitDrugs.add(drug);
        }
        System.out.println("Cached all all NCIT Drugs.");
    }

    private static Set<String> getAcceptedSemanticTypes() {
        List<String> lines = null;
        try {
            lines = FileUtils.readTrimedLinesStream(
                NCITDrugUtils.class.getResourceAsStream("/data/accepted_ncit_semantic_types.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            lines = new ArrayList<>();
        }
        return new HashSet<>(lines);
    }
}

class NCTIDrugComp implements Comparator<NCITDrug> {
    private String keyword;

    public NCTIDrugComp(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public int compare(NCITDrug e1, NCITDrug e2) {
        if (e1.getDrugName().equalsIgnoreCase(keyword)) {
            return -1;
        }
        if (e2.getDrugName().equalsIgnoreCase(keyword)) {
            return 1;
        }
        if (e1.getNcitCode().equalsIgnoreCase(keyword)) {
            return -1;
        }
        if (e2.getNcitCode().equalsIgnoreCase(keyword)) {
            return 1;
        }
        Integer index1 = e1.getDrugName().indexOf(this.keyword);
        Integer index2 = e2.getDrugName().indexOf(this.keyword);
        if (index1.equals(index2)) {
            index1 = e1.getNcitCode().indexOf(this.keyword);
            index2 = e2.getNcitCode().indexOf(this.keyword);
            if (index1.equals(index2)) {
                // In this moment, these are the matches from the synonyms. The order does not matter, so alphabetically sort base on drug name
                return e1.getDrugName().compareTo(e2.getDrugName());
            } else {
                if (index1.equals(-1))
                    return 1;
                if (index2.equals(-1))
                    return -1;
                return index1 - index2;
            }
        } else {
            if (index1.equals(-1))
                return 1;
            if (index2.equals(-1))
                return -1;
            return index1 - index2;
        }
    }
}

