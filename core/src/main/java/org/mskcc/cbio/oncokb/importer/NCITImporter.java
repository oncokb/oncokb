package org.mskcc.cbio.oncokb.importer;

import com.mysql.jdbc.StringUtils;
import org.mskcc.cbio.oncokb.bo.DrugBo;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.mskcc.cbio.oncokb.util.FileUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by Hongxin on 5/26/16.
 */
public final class NCITImporter {

    public static void main(String[] args) throws Exception {
        DrugBo drugBo = ApplicationContextSingleton.getDrugBo();


        System.out.println("getting accepted semantic types...");
        Set<String> acceptedSemanticTypes = getAcceptedSemanticTypes();

        List<String> lines = FileUtils.readTrimedLinesStream(
            NCITImporter.class.getResourceAsStream("/data/Thesaurus.txt"));
        int nLines = lines.size();
        System.out.println("importing...");

        // code <tab> concept name <tab> parents <tab> synonyms <tab> definition <tab> display name <tab> concept status <tab> semantic type <EOL>

        for (int i = 0; i < nLines; i++) {
            if ((i + 1) % 1000 == 0) {
                System.out.println("Imported " + (i + 1));
            }
            String line = lines.get(i);
            if (line.startsWith("#")) continue;

            String[] parts = line.split("\t");

            if (parts.length != 8) {
                System.out.println("The line should have 8 elements: " + Arrays.toString(parts));
                continue;
            }
            String code = parts[0] == null ? null : parts[0].trim();
            String synonyms = parts[3] == null ? null : parts[3].trim();
            String definition = parts[4] == null ? null : parts[4].trim();
            String conceptStatus = parts[6] == null ? null : parts[6].trim();
            String semanticType = parts[7] == null ? null : parts[7].trim();
            List<String> synonymsList = new ArrayList<>();

            if (StringUtils.isNullOrEmpty(code)) {
                System.out.println("The line should have code: " + line);
                continue;
            } else if (!code.startsWith("C")) {
                System.out.println("The code should start with C: " + line);
                continue;
            }

            if (StringUtils.isNullOrEmpty(semanticType)) {
//                System.out.println( "We are only importing lines with semantic types: " + line);
                continue;
            } else {
                Set<String> semanticTypes = new HashSet(Arrays.asList((semanticType.split("\\|"))));
                boolean disjoint = Collections.disjoint(semanticTypes, acceptedSemanticTypes);
                if (disjoint) {
//                    System.out.println("We are only importing lines within accepted semantic types: " + line);
                    continue;
                }
            }

            if (!StringUtils.isNullOrEmpty(conceptStatus)) {
//                System.out.println("We only import item without concept status: " + line);
                continue;
            }

            if (StringUtils.isNullOrEmpty(synonyms)) {
                System.out.println("The line should have synonyms: " + line);
                continue;
            } else {
                // Arrays.asList returning a fixed-size list, you cannot remove item from the list, need to reinitialize a new array list
                synonymsList = new ArrayList<>(Arrays.asList((synonyms.split("\\|"))));
            }

            Drug drug = new Drug();
            drug.setNcitCode(code);
            if (!synonymsList.isEmpty()) {
                drug.setDrugName(synonymsList.get(0));
                synonymsList.remove(0);
                drug.setSynonyms(new HashSet<>(synonymsList));
//                System.out.println("Saving: " + line);
                if (!StringUtils.isNullOrEmpty(definition)) {
                    drug.setDescription(definition);
                }
                drugBo.save(drug);
            }
        }
    }

    private static Set<String> getAcceptedSemanticTypes() throws IOException {
        List<String> lines = FileUtils.readTrimedLinesStream(
            NCITImporter.class.getResourceAsStream("/data/accepted_ncit_semantic_types.txt"));
        return new HashSet<>(lines);
    }
}
