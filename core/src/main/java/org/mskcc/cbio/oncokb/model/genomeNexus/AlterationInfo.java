package org.mskcc.cbio.oncokb.model.genomeNexus;

import org.mskcc.cbio.oncokb.model.Alteration;

public class AlterationInfo {
    private Alteration alteration;
    private String message;

    public AlterationInfo(Alteration alteration, String message) {
        this.alteration = alteration;
        this.message = message;
    }

    public Alteration getAlteration() {
        return alteration;
    }

    public void setAlteration(Alteration alteration) {
        this.alteration = alteration;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
