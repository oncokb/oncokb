package org.mskcc.cbio.oncokb.util;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.NCITDrug;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin Zhang on 10/18/18.
 */
public class NCITDrugUtils {
    private static Set<NCITDrug> allNcitDrugs = null;

    public static NCITDrug findDrugByNcitCode(String ncitCode) {
        if (allNcitDrugs == null) {
            cacheDrugs();
        }

        for (NCITDrug drug : allNcitDrugs) {
            if (drug.getNcitCode().equals(ncitCode)) {
                return drug;
            }
        }
        return null;
    }

    public static LinkedHashSet<NCITDrug> findDrugs(String query) {
        if (allNcitDrugs == null) {
            cacheDrugs();
        }
        return findMatches(query);
    }

    private static LinkedHashSet<NCITDrug> findMatches(String query) {
        TreeSet<NCITDrug> matches = new TreeSet<>(new NCTIDrugComp(query));

        if (query == null || allNcitDrugs == null) {
            return new LinkedHashSet<>();
        }
        query = query.toLowerCase();
        for (NCITDrug drug : allNcitDrugs) {
            if (drug.getNcitCode().toLowerCase().contains(query)) {
                matches.add(drug);
                continue;
            }
            if (drug.getDrugName().toLowerCase().contains(query)) {
                matches.add(drug);
                continue;
            }

            boolean found = false;
            for (String synonym : drug.getSynonyms()) {
                if (synonym.toLowerCase().contains(query)) {
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

        allNcitDrugs = new HashSet<>();

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

            String code = parts[0] == null ? null : parts[0].trim();
            String preferredName = parts[1] == null ? null : parts[1].trim();
            String synonyms = parts.length >= 3 ? (parts[2] == null ? null : parts[2].trim()) : null;
            List<String> synonymsList = new ArrayList<>();

            if (StringUtils.isNullOrEmpty(code)) {
                System.out.println("code is empty: " + line);
                continue;
            } else if (!code.startsWith("C")) {
                continue;
            }

            if (StringUtils.isNullOrEmpty(preferredName)) {
                System.out.println("Preferred name is empty: " + line);
                continue;
            }

            if (synonyms == null) {
                System.out.println("Synonyms is empty: " + line);
                continue;
            } else {
                // Arrays.asList returning a fixed-size list, you cannot remove item from the list, need to reinitialize a new array list
                synonymsList = new ArrayList<>(Arrays.asList((synonyms.split("\\|"))));
            }

            NCITDrug drug = new NCITDrug();
            drug.setNcitCode(code);
            drug.setDrugName(preferredName);
            if (!synonymsList.isEmpty()) {
                synonymsList.remove(preferredName);
                drug.setSynonyms(new HashSet<>(synonymsList));

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

