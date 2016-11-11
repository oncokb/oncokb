package org.mskcc.cbio.oncokb.model;

/**
 * @author Selcuk Onur Sumer
 */
public class IntegerRange {
    private Integer start;
    private Integer end;

    public IntegerRange() {
        this(null, null);
    }

    public IntegerRange(Integer start) {
        this(start, null);
    }

    public IntegerRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }
}
