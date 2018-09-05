package org.mskcc.cbio.oncokb.model;

/**
 * @author Hongxin Zhang
 */
public enum AlterationPositionBoundary {
    START(-1),
    END(100000);

    AlterationPositionBoundary(int position) {
        this.position = position;
    }

    public int getValue() {
        return this.position;
    }

    private int position;
}
