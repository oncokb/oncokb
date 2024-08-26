package org.mskcc.cbio.oncokb.model.epic;

import java.util.ArrayList;

public class Extension {

    private ValueCodeableConcept valueCodeableConcept;
    private String url;
    private ArrayList<Extension> extension;
    private String valueString;
    private boolean valueBoolean;

    public ValueCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    public void setValueCodeableConcept(ValueCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<Extension> getExtension() {
        return extension;
    }

    public void setExtension(ArrayList<Extension> extension) {
        this.extension = extension;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public boolean isValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }
}
