package org.mskcc.cbio.oncokb.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "info")
public class Info implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "oncotree_version")
    String oncoTreeVersion;

    @Column(name = "ncit_version")
    String ncitVersion;

    @Column(name = "data_version")
    String dataVersion;

    @Column(name = "data_version_date")
    Date dataVersionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOncoTreeVersion() {
        return oncoTreeVersion;
    }

    public void setOncoTreeVersion(String oncoTreeVersion) {
        this.oncoTreeVersion = oncoTreeVersion;
    }

    public String getNcitVersion() {
        return ncitVersion;
    }

    public void setNcitVersion(String ncitVersion) {
        this.ncitVersion = ncitVersion;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Date getDataVersionDate() {
        return dataVersionDate;
    }

    public void setDataVersionDate(Date dataVersionDate) {
        this.dataVersionDate = dataVersionDate;
    }

}
