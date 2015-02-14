package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class GoogleUser {
    private String name;
    private String email;
    private Integer permission;
    private String genes;

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
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
}
