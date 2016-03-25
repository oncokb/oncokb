package org.mskcc.cbio.oncokb.Observer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hongxin on 4/1/16.
 */
public class SummaryCacheObserver extends CacheObserver {
    Map<String, Map<String, String>> variantSummary = new HashMap<>();

    public SummaryCacheObserver(CacheSubject subject) {
        this.subject = subject;
        this.subject.attach(this);
    }

    public String getSummary(String gene, String variant) {
        if (variantSummary.containsKey(gene) && variantSummary.get(gene).containsKey(variant)) {
            return variantSummary.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public Boolean containSummary(String gene, String variant) {
        if (variantSummary.containsKey(gene) && variantSummary.get(gene).containsKey(variant)) {
            return true;
        } else {
            return false;
        }
    }

    public void setSummary(String gene, String variant, String summary) {
        if (!variantSummary.containsKey(gene)) {
            variantSummary.put(gene, new HashMap<String, String>());
        }
        variantSummary.get(gene).put(variant, summary);
    }

    @Override
    public void update(String gene) {
        variantSummary.put(gene, new HashMap<String, String>());
    }
}
