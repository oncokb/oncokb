package org.mskcc.cbio.oncokb.validation;

/**
 * Created by Hongxin on 4/21/17.
 */
public class Ensembl {
    String id;
    String seq;
    String molecule;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }
}
