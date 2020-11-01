package org.mskcc.cbio.oncokb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by hongxinzhang on 4/3/16.
 */


public class GeneObservable extends Observable {
    private static GeneObservable instance = new GeneObservable();

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

    public static GeneObservable getInstance() {
        return instance;
    }
}
