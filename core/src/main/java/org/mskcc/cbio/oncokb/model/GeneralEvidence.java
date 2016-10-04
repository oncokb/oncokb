package org.mskcc.cbio.oncokb.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.Objects;


/**
 * GeneralEvidence
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-24T14:50:12.441Z")

public class GeneralEvidence {
    private Integer evidenceId = null;

    private EvidenceType evidenceType = null;

    private String shortDesc = null;

    private String desc = null;

    private String id = null;

    private Date lastEdit = null;

    private String status = null;

    public GeneralEvidence evidenceId(Integer evidenceId) {
        this.evidenceId = evidenceId;
        return this;
    }

    /**
     * Get evidenceId
     *
     * @return evidenceId
     **/
    @ApiModelProperty(required = true, value = "")
    public Integer getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Integer evidenceId) {
        this.evidenceId = evidenceId;
    }

    public GeneralEvidence evidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
        return this;
    }

    /**
     * Get evidenceType
     *
     * @return evidenceType
     **/
    @ApiModelProperty(required = true, value = "")
    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
    }

    public GeneralEvidence shortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
        return this;
    }

    /**
     * Get shortDesc
     *
     * @return shortDesc
     **/
    @ApiModelProperty(value = "")
    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public GeneralEvidence desc(String desc) {
        this.desc = desc;
        return this;
    }

    /**
     * Get desc
     *
     * @return desc
     **/
    @ApiModelProperty(value = "")
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public GeneralEvidence id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @ApiModelProperty(value = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeneralEvidence lastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
        return this;
    }

    /**
     * Get lastEdit
     *
     * @return lastEdit
     **/
    @ApiModelProperty(value = "")
    public Date getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Date lastEdit) {
        this.lastEdit = lastEdit;
    }

    public GeneralEvidence status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @ApiModelProperty(value = "")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneralEvidence evidence = (GeneralEvidence) o;
        return Objects.equals(this.evidenceId, evidence.evidenceId) &&
            Objects.equals(this.evidenceType, evidence.evidenceType) &&
            Objects.equals(this.shortDesc, evidence.shortDesc) &&
            Objects.equals(this.desc, evidence.desc) &&
            Objects.equals(this.id, evidence.id) &&
            Objects.equals(this.lastEdit, evidence.lastEdit) &&
            Objects.equals(this.status, evidence.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(evidenceId, evidenceType, shortDesc, desc, id, lastEdit, status);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GeneralEvidence {\n");

        sb.append("    evidenceId: ").append(toIndentedString(evidenceId)).append("\n");
        sb.append("    evidenceType: ").append(toIndentedString(evidenceType)).append("\n");
        sb.append("    shortDesc: ").append(toIndentedString(shortDesc)).append("\n");
        sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    lastEdit: ").append(toIndentedString(lastEdit)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

