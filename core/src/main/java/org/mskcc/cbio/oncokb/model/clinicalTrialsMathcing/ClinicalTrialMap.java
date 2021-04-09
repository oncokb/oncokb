package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import java.util.Date;
import java.util.List;

/**
 * Created by Yifu Yao on 2020-09-08
 */


public class ClinicalTrialMap {
    private String briefTitle;
    private String nctId;
    private List<Arms> arms;
    private String principalInvestigator;
    private List<String> sites;
    private String currentTrialStatus;
    private Date currentTrialStatusDate;
    private String previousTrialStatus;
    private Date previousTrialStatusDate;
    private String phase;
    private Eligibility eligibility;
    private List<Collaborator> collaborators;
    public String getBriefTitle() {
        return briefTitle;
    }
    public void setBriefTitle(String briefTitle) {
        this.briefTitle = briefTitle;
    }
    public String getNctId() {
        return nctId;
    }
    public void setNctId(String nctId) {
        this.nctId = nctId;
    }
    public List<Arms> getArms() {
        return arms;
    }
    public void setArms(List<Arms> arms) {
        this.arms = arms;
    }
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }
    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }
    public List<String> getSites() {
        return sites;
    }
    public void setSites(List<String> sites) {
        this.sites = sites;
    }
    public String getCurrentTrialStatus() {
        return currentTrialStatus;
    }
    public void setCurrentTrialStatus(String currentTrialStatus) {
        this.currentTrialStatus = currentTrialStatus;
    }
    public Date getCurrentTrialStatusDate() {
        return currentTrialStatusDate;
    }
    public void setCurrentTrialStatusDate(Date currentTrialStatusDate) {
        this.currentTrialStatusDate = currentTrialStatusDate;
    }
    public String getPreviousTrialStatus() {
        return previousTrialStatus;
    }
    public void setPreviousTrialStatus(String previousTrialStatus) {
        this.previousTrialStatus = previousTrialStatus;
    }
    public Date getPreviousTrialStatusDate() {
        return previousTrialStatusDate;
    }
    public void setPreviousTrialStatusDate(Date previousTrialStatusDate) {
        this.previousTrialStatusDate = previousTrialStatusDate;
    }
    public String getPhase() {
        return phase;
    }
    public void setPhase(String phase) {
        this.phase = phase;
    }
    public Eligibility getEligibility() {
        return eligibility;
    }
    public void setEligibility(Eligibility eligibility) {
        this.eligibility = eligibility;
    }
    public List<Collaborator> getCollaborators() {
        return collaborators;
    }
    public void setCollaborators(List<Collaborator> collaborators) {
        this.collaborators = collaborators;
    }

 

}
