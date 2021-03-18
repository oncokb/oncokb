package org.mskcc.cbio.oncokb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class ClinicalTrialsObservable extends Observable{
    private static ClinicalTrialsObservable instance = new ClinicalTrialsObservable();

    public void setChanged() {
        super.setChanged();
    }

    public void update(String cmd, String value) {
        if(cmd != null) {
            Map<String, String> operation = new HashMap<>();
            operation.put("cmd", cmd);
            operation.put("val", value);
            setChanged();
            notifyObservers(operation);
        }
    }

    public static ClinicalTrialsObservable getInstance() {
        return instance;
    }
}
