package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


@ApiModel(description = "")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringMVCServerCodegen", date = "2016-05-08T23:17:19.384Z")
public class MainNumberLevel {

    private Integer number = null;
    private String level = null;


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("number")
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }


    /**
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("level")
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MainNumberLevel mainNumberLevel = (MainNumberLevel) o;
        return Objects.equals(number, mainNumberLevel.number) &&
            Objects.equals(level, mainNumberLevel.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MainNumberLevel {\n");

        sb.append("  number: ").append(number).append("\n");
        sb.append("  level: ").append(level).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
