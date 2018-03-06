

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleAbstract)) return false;

        ArticleAbstract that = (ArticleAbstract) o;

        if (getAbstractContent() != null ? !getAbstractContent().equals(that.getAbstractContent()) : that.getAbstractContent() != null)
            return false;
        return getLink() != null ? getLink().equals(that.getLink()) : that.getLink() == null;
    }

    @Override
    public int hashCode() {
        int result = getAbstractContent() != null ? getAbstractContent().hashCode() : 0;
        result = 31 * result + (getLink() != null ? getLink().hashCode() : 0);
        return result;
    }
}
