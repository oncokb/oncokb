

package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hongxin Zhang
 */
public class ArticleAbstract implements java.io.Serializable {
    @JsonProperty("abstract")
    private String abstractContent;
    private String link;

    public String getAbstractContent() {
        return abstractContent;
    }

    public void setAbstractContent(String abstractContent) {
        this.abstractContent = abstractContent;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ArticleAbstract() {
    }
}
