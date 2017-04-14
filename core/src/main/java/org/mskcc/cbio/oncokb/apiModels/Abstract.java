package org.mskcc.cbio.oncokb.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Hongxin on 4/12/17.
 */
public class Abstract {
    @JsonProperty("abstract")
    private String abstractContent;
    private String link;

    public Abstract(String abstractContent) {
        this.abstractContent = abstractContent;
    }

    public Abstract(String abstractContent, String link) {
        this.abstractContent = abstractContent;
        this.link = link;
    }

    public String getAbstractContent() {
        return abstractContent;
    }

    public void setAbstractContent(String abstractContent) {
        this.abstractContent = abstractContent;
    }
}
