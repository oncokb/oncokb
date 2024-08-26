package org.mskcc.cbio.oncokb.model.epic;

import java.util.ArrayList;

public class ValueCodeableConcept {

    private ArrayList<Coding> coding;
    private String text;

    public ArrayList<Coding> getCoding() {
        return coding;
    }

    public void setCoding(ArrayList<Coding> coding) {
        this.coding = coding;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
