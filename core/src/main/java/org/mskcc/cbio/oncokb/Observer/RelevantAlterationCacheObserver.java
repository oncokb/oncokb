package org.mskcc.cbio.oncokb.Observer;

import org.mskcc.cbio.oncokb.model.Alteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hongxin on 4/1/16.
 */
public class RelevantAlterationCacheObserver extends CacheObserver {
    Map<String, Map<String, List<Alteration>>> alterations = new HashMap<>();

    public RelevantAlterationCacheObserver(CacheSubject subject) {
        this.subject = subject;
        this.subject.attach(this);
    }

    public List<Alteration> getAlterations(String gene, String variant) {
        if (alterations.containsKey(gene) && alterations.get(gene).containsKey(variant)) {
            return alterations.get(gene).get(variant);
        } else {
            return null;
        }
    }

    public Boolean containAlterations(String gene, String variant) {
        if (alterations.containsKey(gene) && alterations.get(gene).containsKey(variant)) {
            return true;
        } else {
            return false;
        }
    }

    public void setAlterations(String gene, String variant, List<Alteration> alts) {
        if(!alterations.containsKey(gene)) {
            alterations.put(gene, new HashMap<String, List<Alteration>>());
        }
        alterations.get(gene).put(variant, alts);
    }

    @Override
    public void update(String gene) {
        alterations.put(gene, new HashMap<String, List<Alteration>>());
    }
}
