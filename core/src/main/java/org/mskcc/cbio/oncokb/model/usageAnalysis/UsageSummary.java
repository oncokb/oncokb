package org.mskcc.cbio.oncokb.model.usageAnalysis;

import java.util.Map;

/**
 * Created by Yifu Yao on 2020-10-28
 */

public class UsageSummary {
    private Map<String, Map<String, Integer>> month;
    private Map<String, Integer> year;

    public Map<String, Map<String, Integer>> getMonth() {
        return month;
    }

    public void setMonth(Map<String, Map<String, Integer>> month) {
        this.month = month;
    }

    public Map<String, Integer> getYear() {
        return year;
    }

    public void setYear(Map<String, Integer> year) {
        this.year = year;
    }


}
