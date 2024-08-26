package org.mskcc.cbio.oncokb.model.epic;

import java.util.ArrayList;
import java.util.Date;

public class Resource {

    private String resourceType;
    private String id;
    private ArrayList<BasedOn> basedOn;
    private String status;
    private ArrayList<Category> category;
    private Code code;
    private Subject subject;
    private Date effectiveDateTime;
    private Date issued;
    private ArrayList<HasMember> hasMember;
    private ArrayList<Extension> extension;
    private ValueCodeableConcept valueCodeableConcept;
    private ArrayList<Component> component;
    private ArrayList<DerivedFrom> derivedFrom;
    
    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ArrayList<BasedOn> getBasedOn() {
        return basedOn;
    }
    public void setBasedOn(ArrayList<BasedOn> basedOn) {
        this.basedOn = basedOn;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public ArrayList<Category> getCategory() {
        return category;
    }
    public void setCategory(ArrayList<Category> category) {
        this.category = category;
    }
    public Code getCode() {
        return code;
    }
    public void setCode(Code code) {
        this.code = code;
    }
    public Subject getSubject() {
        return subject;
    }
    public void setSubject(Subject subject) {
        this.subject = subject;
    }
    public Date getEffectiveDateTime() {
        return effectiveDateTime;
    }
    public void setEffectiveDateTime(Date effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }
    public Date getIssued() {
        return issued;
    }
    public void setIssued(Date issued) {
        this.issued = issued;
    }
    public ArrayList<HasMember> getHasMember() {
        return hasMember;
    }
    public void setHasMember(ArrayList<HasMember> hasMember) {
        this.hasMember = hasMember;
    }
    public ArrayList<Extension> getExtension() {
        return extension;
    }
    public void setExtension(ArrayList<Extension> extension) {
        this.extension = extension;
    }
    public ValueCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }
    public void setValueCodeableConcept(ValueCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }
    public ArrayList<Component> getComponent() {
        return component;
    }
    public void setComponent(ArrayList<Component> component) {
        this.component = component;
    }
    public ArrayList<DerivedFrom> getDerivedFrom() {
        return derivedFrom;
    }
    public void setDerivedFrom(ArrayList<DerivedFrom> derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    
}
