package org.mskcc.cbio.oncokb.model.epic;

import java.util.ArrayList;

public class Component {

    private Code code;
    private ValueCodeableConcept valueCodeableConcept;
    private ArrayList<Extension> extension;
    private String valueString;
    private ValueRange valueRange;

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public ValueCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    public void setValueCodeableConcept(ValueCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
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

    public ValueRange getValueRange() {
        return valueRange;
    }

    public void setValueRange(ValueRange valueRange) {
        this.valueRange = valueRange;
    }
}
