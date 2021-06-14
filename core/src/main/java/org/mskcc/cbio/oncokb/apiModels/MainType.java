package org.mskcc.cbio.oncokb.apiModels;

import io.swagger.annotations.ApiModel;
import org.mskcc.cbio.oncokb.model.TumorForm;

import java.io.Serializable;
import java.util.Objects;


/**
 * General tumor type category.
 **/
@ApiModel(description = "OncoTree Cancer Type")
public class MainType implements Serializable {

  private Integer id = null;
  private String name = null;
  private TumorForm tumorForm = null;

  public MainType(){}

  public MainType(String name) {
      this.name = name;
  }

  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public TumorForm getTumorForm() {
      return tumorForm;
  }

    public void setTumorForm(TumorForm tumorForm) {
        this.tumorForm = tumorForm;
    }

    @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MainType mainType = (MainType) o;
    return Objects.equals(id, mainType.id) &&
        Objects.equals(name, mainType.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class MainType {\n");

    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  tumor form: ").append(tumorForm).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
