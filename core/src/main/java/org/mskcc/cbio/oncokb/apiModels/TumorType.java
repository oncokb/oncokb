package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.mskcc.cbio.oncokb.model.TumorForm;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@ApiModel(description = "OncoTree Detailed Cancer Type. See https://oncotree.mskcc.org/?version=oncotree_2019_12_01")
public class TumorType implements Serializable {

    @ApiModelProperty(value = "Database TumorType ID")
    private Integer id = null;
    @ApiModelProperty(value = "Oncotree code")
    private String code = null;
    @ApiModelProperty(value = "(Nullable) Color of tumor")
    private String color = null;
    @ApiModelProperty(value = "(Nullable) Oncotree sub type")
    private String name = null;
    @ApiModelProperty(value = "(Nullable) Oncotree tumor main type")
    private MainType mainType = null;
    @ApiModelProperty(value = "(Nullable) Oncotree tumor tissue")
    private String tissue = null;
    @ApiModelProperty(value = "Map from parent tumor type to children tumor types")
    private Map<String, TumorType> children = new HashMap<String, TumorType>();
    @ApiModelProperty(value = "(Nullable) Parent tumor name")
    private String parent = null;
    @ApiModelProperty(value = "Oncotree tumor type level. -1 indicates special tumor types (See SpecialTumorTypes.java)")
    private Integer level = null;
    @ApiModelProperty(value = "Tumor form")
    private TumorForm tumorForm = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MainType getMainType() {
        return mainType;
    }

    public void setMainType(MainType mainType) {
        this.mainType = mainType;
    }

    public String getTissue() {
        return tissue;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }

    public Map<String, TumorType> getChildren() {
        return children;
    }

    public void setChildren(Map<String, TumorType> children) {
        this.children = children;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public TumorForm getTumorForm() {
        return tumorForm;
    }

    public void setTumorForm(TumorForm tumorForm) {
        this.tumorForm = tumorForm;
    }

    public TumorType() {
    }

    public TumorType(org.mskcc.oncotree.model.TumorType oncoTreeTumorType) {
        this.setName(oncoTreeTumorType.getName());
        this.setTissue(oncoTreeTumorType.getTissue());
        this.setCode(oncoTreeTumorType.getCode());
        this.setColor(oncoTreeTumorType.getColor());
        MainType mainType = new MainType();
        mainType.setName(oncoTreeTumorType.getMainType());
        this.setMainType(mainType);
        this.setParent(oncoTreeTumorType.getParent());
        this.setLevel(oncoTreeTumorType.getLevel());
        Map<String, TumorType> children = new HashMap<>();
        for(Map.Entry<String, org.mskcc.oncotree.model.TumorType> entry: oncoTreeTumorType.getChildren().entrySet()) {
            children.put(entry.getKey(), new TumorType(entry.getValue()));
        }
        this.setChildren(children);
        this.setTissue(oncoTreeTumorType.getTissue());
    }

    // do not include parent and child
    public TumorType(org.mskcc.cbio.oncokb.model.TumorType tumorType) {
        this.setId(tumorType.getId());
        this.setName(tumorType.getSubtype());
        this.setTissue(tumorType.getTissue());
        this.setCode(tumorType.getCode());
        this.setColor(tumorType.getColor());
        if (tumorType.getMainType() != null) {
            MainType mainType = new MainType();
            mainType.setName(tumorType.getMainType());
            org.mskcc.cbio.oncokb.model.TumorType mainTumorType = ApplicationContextSingleton.getTumorTypeBo().getByMainType(tumorType.getMainType());
            mainType.setTumorForm(mainTumorType.getTumorForm());
            this.setMainType(mainType);
        }
        if (tumorType.getParent() != null) {
            this.setParent(tumorType.getParent().getCode());
        }
        this.setLevel(tumorType.getLevel());
        this.setTissue(tumorType.getTissue());
        this.setTumorForm(tumorType.getTumorForm());
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
                Objects.equals(getName(), tumorType.getName()) &&
                Objects.equals(getMainType(), tumorType.getMainType());
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId();
        } else {
            return Objects.hash(getCode(), getName(), getMainType());
        }
    }

    @Override
    public String toString() {
        return "TumorType{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", color='" + color + '\'' +
            ", name='" + name + '\'' +
            ", mainType=" + mainType +
            ", tissue='" + tissue + '\'' +
            ", children=" + children +
            ", parent='" + parent + '\'' +
            ", level=" + level +
            ", tumorForm=" + tumorForm +
            '}';
    }
}
