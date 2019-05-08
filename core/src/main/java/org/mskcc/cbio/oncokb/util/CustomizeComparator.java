package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.IndicatorQueryTreatment;
import org.mskcc.cbio.oncokb.model.LevelOfEvidence;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hongxin Zhang on 2/20/18.
 */
public class CustomizeComparator {
    private static final List<String> KIT_TREATMENT_ORDER = Collections.unmodifiableList(
        Arrays.asList("Imatinib", "Sunitinib", "Regorafenib", "Sorafenib")
    );


    public static void sortEvidenceBasedOnPriority(List<Evidence> evidenceList, final List<LevelOfEvidence> levels) {
        if (evidenceList == null)
            return;
        Collections.sort(evidenceList, new Comparator<Evidence>() {
            public int compare(Evidence e1, Evidence e2) {
                Integer comparison = LevelUtils.compareLevel(e1.getLevelOfEvidence(), e2.getLevelOfEvidence(), levels);

                if (comparison != 0) {
                    return comparison;
                }

                // Compare the highest priority of each evidence
                comparison = e1.getHighestTreatmentPriority() - e2.getHighestTreatmentPriority();

                if (comparison != 0) {
                    return comparison;
                }

                if (e1.getId() == null) {
                    if (e2.getId() == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                if (e2.getId() == null)
                    return -1;
                return e1.getId() - e2.getId();
            }
        });
    }

    public static void sortKitTreatment(List<IndicatorQueryTreatment> treatments) {
        if (treatments == null)
            return;
        Collections.sort(treatments, new Comparator<IndicatorQueryTreatment>() {
            public int compare(IndicatorQueryTreatment t1, IndicatorQueryTreatment t2) {
                if (t1.getLevel() != null
                    && t2.getLevel() != null
                    && t1.getLevel().equals(t2.getLevel())
                    && t1.getDrugs() != null
                    && t2.getDrugs() != null
                    && t1.getDrugs().size() == 1
                    && t2.getDrugs().size() == 1
                    ) {
                    return compareKitTreatmentOrder(
                        t1.getDrugs().get(0).getDrugName(),
                        t2.getDrugs().get(0).getDrugName()
                    );
                } else {
                    return 0;
                }
            }
        });
    }

    public static void sortKitTreatmentByEvidence(List<Evidence> evidences) {
        if (evidences == null)
            return;
        Collections.sort(evidences, new Comparator<Evidence>() {
            public int compare(Evidence e1, Evidence e2) {
                if (e1.getLevelOfEvidence() != null
                    && e2.getLevelOfEvidence() != null
                    && e1.getLevelOfEvidence().equals(e2.getLevelOfEvidence())
                    && e1.getTreatments() != null
                    && e2.getTreatments() != null) {
                    return compareKitTreatmentOrder(
                        TreatmentUtils.getTreatmentName(e1.getTreatments()),
                        TreatmentUtils.getTreatmentName(e2.getTreatments())
                    );
                } else {
                    return 0;
                }
            }
        });
    }

    private static int compareKitTreatmentOrder(String n1, String n2) {
        int i1 = KIT_TREATMENT_ORDER.indexOf(n1);
        int i2 = KIT_TREATMENT_ORDER.indexOf(n2);
        if (i1 == i2)
            return 0;
        if (i1 == -1)
            return 1;
        if (i2 == -1)
            return -1;
        return i1 - i2;
    }
}
