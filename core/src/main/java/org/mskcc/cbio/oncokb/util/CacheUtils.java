package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.Observer.AlterationCacheObserver;
import org.mskcc.cbio.oncokb.Observer.CacheSubject;
import org.mskcc.cbio.oncokb.Observer.RelevantAlterationCacheObserver;
import org.mskcc.cbio.oncokb.Observer.SummaryCacheObserver;
import org.mskcc.cbio.oncokb.model.TumorType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hongxin on 4/1/16.
 */
public class CacheUtils {
    public static CacheSubject subject = new CacheSubject();
    public static SummaryCacheObserver summaryCacheObserver = new SummaryCacheObserver(subject);
    public static AlterationCacheObserver alterationCacheObserver = new AlterationCacheObserver(subject);
    public static RelevantAlterationCacheObserver relevantAlterationCacheObserver = new RelevantAlterationCacheObserver(subject);
    public static Map<String, List<TumorType>> mappedTumorTypes = new HashMap<>();

}
