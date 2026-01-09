package org.mskcc.cbio.oncokb.util.parser;

import org.mskcc.cbio.oncokb.model.AlterationPositionBoundary;

public class ParseAlterationResult {
    String consequence = "NA";
    String ref = null;
    String var = null;
    Integer start = AlterationPositionBoundary.START.getValue();
    Integer end = AlterationPositionBoundary.END.getValue();
    String normalizedProteinChange = "";
    Boolean isParsed = false;
    
    public ParseAlterationResult() {}

    public String getConsequence() {
        return this.consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }

    public String getRef() {
        return this.ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getVar() {
        return this.var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public Integer getStart() {
        return this.start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return this.end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getNormalizedProteinChange() {
        return this.normalizedProteinChange;
    }

    public void setNormalizedProteinChange(String normalizedProteinChange) {
        this.normalizedProteinChange = normalizedProteinChange;
    }

    public Boolean getIsParsed() {
        return this.isParsed;
    }

    public void setIsParsed(Boolean isParsed) {
        this.isParsed = isParsed;
    }

}
