package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

/**
 * Created by Yifu Yao on 3/16/2021
 */
public class Collaborator {
    private String functionalRole;
    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFunctionalRole() {
        return functionalRole;
    }
    public void setFunctionalRole(String functionalRole) {
        this.functionalRole = functionalRole;
    }

}
