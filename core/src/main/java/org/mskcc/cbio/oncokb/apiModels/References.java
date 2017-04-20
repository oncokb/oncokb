package org.mskcc.cbio.oncokb.apiModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mskcc.cbio.oncokb.model.Article;

import java.util.List;

/**
 * Created by Hongxin on 4/18/17.
 */
public class References {
    private List<Article> articles;
    @JsonProperty("abstracts")
    private List<Article> abstractList;

    public References() {
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public List<Article> getAbstractList() {
        return abstractList;
    }

    public void setAbstractList(List<Article> abstractList) {
        this.abstractList = abstractList;
    }
}
