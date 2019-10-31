package org.mskcc.cbio.oncokb.apiModels;

/**
 * Created by Hongxin Zhang on 9/23/19.
 */
public class CancerTypeCount {
    String cancerType;
    Integer count;


    public CancerTypeCount(String cancerType, Integer count) {
        this.cancerType = cancerType;
        this.count = count;
    }

    public String getCancerType() {
        return cancerType;
    }

    public void setCancerType(String cancerType) {
        this.cancerType = cancerType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
