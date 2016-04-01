package org.mskcc.cbio.oncokb.Observer;

import org.mskcc.cbio.oncokb.model.Alteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hongxin on 4/1/16.
 */
public class AlterationCacheObserver extends CacheObserver {
    Map<String, List<Alteration>> alterations = new HashMap<>();

    public AlterationCacheObserver(CacheSubject subject) {
        this.subject = subject;
        this.subject.attach(this);
    }

    public List<Alteration> getAlterations(String gene) {
        return alterations.get(gene);
    }

    public void setAlterations(String gene, List<Alteration> alts) {
        alterations.put(gene, alts);
    }

    @Override
    public void update(String gene) {
        alterations.put(gene, new ArrayList<Alteration>());
    }
}
