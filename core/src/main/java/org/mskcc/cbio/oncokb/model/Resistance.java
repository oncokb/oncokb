package org.mskcc.cbio.oncokb.model;

import java.util.*;

public enum Resistance {
    YES("Yes");

    private Resistance(String effect) {
        this.effect = effect;
    }

    private final String effect;

    public String getEffect() {
        return effect;
    }

    private static final Map<String, Resistance> map = new HashMap<String, Resistance>();

    static {
        for (Resistance res : Resistance.values()) {
            map.put(res.getEffect().toLowerCase(), res);
        }
    }

    public static Resistance getByEffect(String effect) {
        if (effect == null) return null;
        return map.get(effect.toLowerCase());
    }

}
