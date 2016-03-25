package org.mskcc.cbio.oncokb.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hongxin on 4/1/16.
 */
public class CacheSubject {
    private List<CacheObserver> observers = new ArrayList<>();
    private String gene;

    public String getState() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
        notifyAllObservers();
    }

    public void attach(CacheObserver observer){
        observers.add(observer);
    }

    public void notifyAllObservers(){
        System.out.println("Start to notify all observers.");
        for (CacheObserver observer : observers) {
            observer.update(gene);
        }
    }
}
