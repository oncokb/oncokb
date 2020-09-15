

package org.mskcc.cbio.oncokb.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jgao
 */
public enum Oncogenicity {
    YES("Oncogenic"),
    LIKELY("Likely Oncogenic"),
    LIKELY_NEUTRAL("Likely Neutral"),
    PREDICTED("Predicted Oncogenic"),
    INCONCLUSIVE("Inconclusive"),
    UNKNOWN("Unknown");

    private Oncogenicity(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    private final String oncogenic;

    public String getOncogenic() {
        return oncogenic;
    }

    private static final Map<String, Oncogenicity> map = new HashMap<String, Oncogenicity>();

    static {
        for (Oncogenicity oncogenicity : Oncogenicity.values()) {
            map.put(oncogenicity.getOncogenic(), oncogenicity);
        }
    }

    public static Oncogenicity getByEffect(String oncogenicity) {
        return map.get(oncogenicity);
    }

    public static int compare(Oncogenicity o1, Oncogenicity o2) {
        //0 indicates o1 has the same oncogenicity with o2
        //positive number indicates o1 has higher oncogenicity than o2
        //negative number indicates o2 has higher oncogenicity than o1
        List<Oncogenicity> oncogenicityValues = new ArrayList<>(Arrays.asList(INCONCLUSIVE, LIKELY_NEUTRAL, LIKELY, YES));
        if (o1 == null && o2 == null) return 0;
        else if (o1 == null) return -1;
        else if (o2 == null) return 1;
        else return oncogenicityValues.indexOf(o1) - oncogenicityValues.indexOf(o2);
    }

    public static Oncogenicity getByEvidence(Evidence evidence) {
        if (evidence != null) {
            return getByEffect(evidence.getKnownEffect());
        }
        return null;
    }
}
