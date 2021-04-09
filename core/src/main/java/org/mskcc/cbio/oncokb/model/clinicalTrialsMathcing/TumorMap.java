package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import java.util.List;

/**
 * Created by Yifu Yao on 2020-09-08
 */
public class TumorMap {
    private String nciCode;
    private String nciMainType;
    private List<String> trials;
    private String tumorName;

    public String getNciCode() {
        return nciCode;
    }

    public void setNciCode(String nciCode) {
        this.nciCode = nciCode;
    }

    public String getNciMainType() {
        return nciMainType;
    }

    public void setNciMainType(String nciMainType) {
        this.nciMainType = nciMainType;
    }

    public List<String> getTrials() {
        return trials;
    }

    public void setTrials(List<String> trials) {
        this.trials = trials;
    }

    public String getTumorName() {
        return tumorName;
    }

    public void setTumorName(String tumorName) {
        this.tumorName = tumorName;
    }
}
