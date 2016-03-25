package org.mskcc.cbio.oncokb.Observer;

/**
 * Created by Hongxin on 4/1/16.
 */
public abstract class CacheObserver {
    protected CacheSubject subject;
    public abstract void update(String gene);
}
