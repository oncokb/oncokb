package org.mskcc.cbio.oncokb.model;

import com.vdurmont.semver4j.Semver;

import java.io.Serializable;

public class SemVer implements Serializable {
    String version;
    Integer major;
    Integer minor;
    Integer patch;
    String[] suffixTokens;
    Boolean stable;

    public SemVer(String version, Semver.SemverType semverType) {
        if (version == null) {
            version = "";
        }
        if (version.startsWith("v")) {
            version = version.substring(1);
        }
        Semver semver = new Semver(version, semverType == null ? Semver.SemverType.STRICT : semverType);
        this.version = "v" + version;
        this.major = semver.getMajor();
        this.minor = semver.getMinor();
        this.patch = semver.getPatch();
        this.suffixTokens = semver.getSuffixTokens();
        this.stable = semver.isStable();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public Integer getPatch() {
        return patch;
    }

    public void setPatch(Integer patch) {
        this.patch = patch;
    }

    public String[] getSuffixTokens() {
        return suffixTokens;
    }

    public void setSuffixTokens(String[] suffixTokens) {
        this.suffixTokens = suffixTokens;
    }

    public Boolean getStable() {
        return stable;
    }

    public void setStable(Boolean stable) {
        this.stable = stable;
    }
}
