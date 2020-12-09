package org.mskcc.cbio.oncokb.model;

import io.swagger.annotations.ApiModel;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@NamedQueries({
    @NamedQuery(
        name = "findTumorTypeByCode",
        query = "select tt from TumorType  tt where tt.code=?"
    ),
})

@Entity
@Table(name = "tumor_type")
@ApiModel(description = "OncoTree Detailed Cancer Type")
public class TumorType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "code")
    private String code;

    @Column(name = "color")
    private String color;

    @Column(name = "main_type", nullable = false)
    private String mainType="";

    @Column(name = "level")
    private Integer level;

    @Column(name = "tissue")
    private String tissue;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "parent")
    private TumorType parent;

    @Column(name = "tumor_form")
    @Enumerated(EnumType.STRING)
    private TumorForm tumorForm = null;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tumor_type_children", joinColumns = {
        @JoinColumn(name = "tumor_type_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
        @JoinColumn(name = "tumor_type_child_id", referencedColumnName = "id")
    })
    private Set<TumorType> children;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMainType() {
        return mainType;
    }

    public void setMainType(String mainType) {
        this.mainType = mainType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getTissue() {
        return tissue;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }

    public TumorType getParent() {
        return parent;
    }

    public void setParent(TumorType parent) {
        this.parent = parent;
    }

    public TumorForm getTumorForm() {
        return tumorForm;
    }

    public void setTumorForm(TumorForm tumorForm) {
        this.tumorForm = tumorForm;
    }

    public Set<TumorType> getChildren() {
        return children;
    }

    public void setChildren(Set<TumorType> children) {
        this.children = children;
    }

    public TumorType() {
    }

    public TumorType(org.mskcc.oncotree.model.TumorType oncoTreeTumorType) {
        this.setName(oncoTreeTumorType.getName());
        this.setTissue(oncoTreeTumorType.getTissue());
        this.setCode(oncoTreeTumorType.getCode());
        this.setColor(oncoTreeTumorType.getColor());
        this.setMainType(oncoTreeTumorType.getMainType());
        this.setLevel(oncoTreeTumorType.getLevel());
        Set<TumorType> children = new HashSet<>();
        for (Map.Entry<String, org.mskcc.oncotree.model.TumorType> entry : oncoTreeTumorType.getChildren().entrySet()) {
            children.add(new TumorType(entry.getValue()));
        }
        this.setChildren(children);
        this.setTissue(oncoTreeTumorType.getTissue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TumorType)) return false;
        TumorType tumorType = (TumorType) o;
        return Objects.equals(getId(), tumorType.getId()) &&
            Objects.equals(getCode(), tumorType.getCode()) &&
            Objects.equals(getName(), tumorType.getName()) &&
            Objects.equals(getMainType(), tumorType.getMainType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCode(), getName(), getMainType());
    }
}
