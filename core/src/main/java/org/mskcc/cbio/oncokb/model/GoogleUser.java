package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class GoogleUser {
    private String name;
    private String email;
    private String mskccEmail;
    private Integer role;
    private String genes;
    private String phases;

    public String getMskccEmail() {
        return mskccEmail;
    }

    public void setMskccEmail(String mskccEmail) {
        this.mskccEmail = mskccEmail;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGenes() {
        return genes;
    }

    public void setGenes(String genes) {
        this.genes = genes;
    }

    public String getPhases() {
        return phases;
    }

    public void setPhases(String phases) {
        this.phases = phases;
    }
}
