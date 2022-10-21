package org.mskcc.cbio.oncokb.model;

import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "cancer_type")
@ApiModel(description = "OncoTree Detailed Cancer Type")
public class TumorType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "subtype", nullable = false)
    private String subtype = "";

    @Column(name = "code", nullable = false)
    private String code = "";

    @Column(name = "color", nullable = false)
    private String color = "";

    @Column(name = "main_type", nullable = false)
    private String mainType = "";

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "tissue", nullable = false)
    private String tissue = "";

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "parent")
    @JsonIgnore
    private TumorType parent;

    @Column(name = "tumor_form")
    @Enumerated(EnumType.STRING)
    private TumorForm tumorForm = null;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "cancer_type_child", joinColumns = {
        @JoinColumn(name = "cancer_type_id", referencedColumnName = "id")
    }, inverseJoinColumns = {
        @JoinColumn(name = "cancer_type_child_id", referencedColumnName = "id")
    })
    @JsonIgnore
    private Set<TumorType> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
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
        if (!StringUtils.isNullOrEmpty(oncoTreeTumorType.getName())) {
            this.setSubtype(oncoTreeTumorType.getName());
        }
        if (!StringUtils.isNullOrEmpty(oncoTreeTumorType.getTissue())) {
            this.setTissue(oncoTreeTumorType.getTissue());
        }
        if (!StringUtils.isNullOrEmpty(oncoTreeTumorType.getCode())) {
            this.setCode(oncoTreeTumorType.getCode());
        }
        if (!StringUtils.isNullOrEmpty(oncoTreeTumorType.getColor())) {
            this.setColor(oncoTreeTumorType.getColor());
        }
        if (!StringUtils.isNullOrEmpty(oncoTreeTumorType.getMainType())) {
            this.setMainType(oncoTreeTumorType.getMainType());
        }
        if (oncoTreeTumorType.getLevel() != null) {
            this.setLevel(oncoTreeTumorType.getLevel());
        }
        Set<TumorType> children = new HashSet<>();
        for (Map.Entry<String, org.mskcc.oncotree.model.TumorType> entry : oncoTreeTumorType.getChildren().entrySet()) {
            children.add(new TumorType(entry.getValue()));
        }
        this.setChildren(children);
    }

    public TumorType(org.mskcc.cbio.oncokb.apiModels.TumorType tumorType) {
        if (!StringUtils.isNullOrEmpty(tumorType.getName())) {
            this.setSubtype(tumorType.getName());
        }
        if (!StringUtils.isNullOrEmpty(tumorType.getTissue())) {
            this.setTissue(tumorType.getTissue());
        }
        if (!StringUtils.isNullOrEmpty(tumorType.getCode())) {
            this.setCode(tumorType.getCode());
        }
        if (!StringUtils.isNullOrEmpty(tumorType.getColor())) {
            this.setColor(tumorType.getColor());
        }
        if (tumorType.getMainType() != null && !StringUtils.isNullOrEmpty(tumorType.getMainType().getName())) {
            this.setMainType(tumorType.getMainType().getName());
        }
        if (tumorType.getLevel() != null) {
            this.setLevel(tumorType.getLevel());
        }
        Set<TumorType> children = new HashSet<>();
        for (Map.Entry<String, org.mskcc.cbio.oncokb.apiModels.TumorType> entry : tumorType.getChildren().entrySet()) {
            children.add(new TumorType(entry.getValue()));
        }
        this.setChildren(children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TumorType)) return false;
        TumorType tumorType = (TumorType) o;
        if (getId() != null && tumorType.getId() != null) {
            return Objects.equals(getId(), tumorType.getId());
        } else {
            return Objects.equals(getCode(), tumorType.getCode()) &&
                Objects.equals(getTissue(), tumorType.getTissue()) &&
                Objects.equals(getLevel(), tumorType.getLevel()) &&
                Objects.equals(getColor(), tumorType.getColor()) &&
                Objects.equals(getSubtype(), tumorType.getSubtype()) &&
                Objects.equals(getMainType(), tumorType.getMainType());
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId();
        } else {
            return Objects.hash(getCode(), getSubtype(), getMainType());
        }
    }
}
