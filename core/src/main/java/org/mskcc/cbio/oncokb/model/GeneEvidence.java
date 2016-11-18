package org.mskcc.cbio.oncokb.model;

import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * GeneEvidence
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-24T14:50:12.441Z")

public class GeneEvidence extends GeneralEvidence {
    private Gene gene = null;
    private Set<Article> articles = new HashSet<>();

    /**
     * Get gene
     *
     * @return gene
     **/
    @ApiModelProperty(value = "")
    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
    }

    public GeneEvidence articles(Set<Article> articles) {
        this.articles = articles;
        return this;
    }

    public GeneEvidence addArticlesItem(Article articlesItem) {
        this.articles.add(articlesItem);
        return this;
    }

    /**
     * Get articles
     *
     * @return articles
     **/
    @ApiModelProperty(value = "")
    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeneEvidence geneEvidence = (GeneEvidence) o;
        return Objects.equals(this.gene, geneEvidence.gene) &&
            Objects.equals(this.articles, geneEvidence.articles) &&
            super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gene, articles, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GeneEvidence {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    gene: ").append(toIndentedString(gene)).append("\n");
        sb.append("    articles: ").append(toIndentedString(articles)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public GeneEvidence(Evidence e) {
        this.setGene(e.getGene());
        this.setEvidenceId(e.getId());
        this.setEvidenceType(e.getEvidenceType());
        this.setLastEdit(e.getLastEdit());
        this.setArticles(e.getArticles());
        this.setDesc(e.getDescription());
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

