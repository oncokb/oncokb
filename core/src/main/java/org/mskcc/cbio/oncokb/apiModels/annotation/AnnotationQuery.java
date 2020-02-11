package org.mskcc.cbio.oncokb.apiModels.annotation;

import org.mskcc.cbio.oncokb.model.EvidenceType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public class AnnotationQuery implements java.io.Serializable{
    private String id; //Optional, This id is passed from request. The identifier used to distinguish the query
    private String tumorType;
    private Set<EvidenceType> evidenceTypes = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTumorType() {
        return tumorType;
    }

    public void setTumorType(String tumorType) {
        this.tumorType = tumorType;
    }

    public Set<EvidenceType> getEvidenceTypes() {
        return evidenceTypes;
    }

    public void setEvidenceTypes(Set<EvidenceType> evidenceTypes) {
        this.evidenceTypes = evidenceTypes;
    }
}
