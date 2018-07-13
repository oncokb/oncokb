

package org.mskcc.cbio.oncokb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author jgao
 */

@NamedQueries({
    @NamedQuery(
        name = "findArticleByPmid",
        query = "select a from Article a where a.pmid=?"
    ),
    @NamedQuery(
        name = "findArticleByAbstract",
        query = "select a from Article a where a.abstractContent=?"
    )
})

@Entity
@Table(name = "article")
public class Article implements java.io.Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer id;

    @Column(length = 40)
    @JsonIgnore
    private String uuid;

    private String pmid;

    @Column(length = 1000)
    private String title;
    private String journal;

    @Column(name = "pub_date")
    private String pubDate;
    private String volume;
    private String issue;
    private String pages;
    private String authors;

    private String elocationId;

    @Column(name = "abstract_content")
    private String abstractContent;

    @Column(length = 500)
    private String link;

    @JsonProperty("abstract")
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

    public Article() {
    }

    public Article(String pmid) {
        this.pmid = pmid;
    }

    public Article(String pmid, String title, String journal, String pubDate, String volume, String issue, String pages, String authors, String elocationId, String abstractContent, String link) {
        this.pmid = pmid;
        this.title = title;
        this.journal = journal;
        this.pubDate = pubDate;
        this.volume = volume;
        this.issue = issue;
        this.pages = pages;
        this.authors = authors;
        this.elocationId = elocationId;
        this.abstractContent = abstractContent;
        this.link = link;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getElocationId() {
        return elocationId;
    }

    public void setElocationId(String elocationId) {
        this.elocationId = elocationId;
    }

    public String getReference() {
        StringBuilder sb = new StringBuilder();
        sb.append(authors).append(". ")
            .append(journal).append(". ");
        if (pubDate != null)
            sb.append(pubDate).append(";");
        if (volume != null)
            sb.append(volume);
        if (issue != null)
            sb.append("(").append(issue).append(")");
        if (pages != null)
            sb.append(pages);
        sb.append(".");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.pmid);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (getUuid() != null ? !getUuid().equals(article.getUuid()) : article.getUuid() != null) return false;
        if (getPmid() != null ? !getPmid().equals(article.getPmid()) : article.getPmid() != null) return false;
        if (getTitle() != null ? !getTitle().equals(article.getTitle()) : article.getTitle() != null) return false;
        if (getJournal() != null ? !getJournal().equals(article.getJournal()) : article.getJournal() != null)
            return false;
        if (getPubDate() != null ? !getPubDate().equals(article.getPubDate()) : article.getPubDate() != null)
            return false;
        if (getVolume() != null ? !getVolume().equals(article.getVolume()) : article.getVolume() != null) return false;
        if (getIssue() != null ? !getIssue().equals(article.getIssue()) : article.getIssue() != null) return false;
        if (getPages() != null ? !getPages().equals(article.getPages()) : article.getPages() != null) return false;
        if (getAuthors() != null ? !getAuthors().equals(article.getAuthors()) : article.getAuthors() != null)
            return false;
        if (getElocationId() != null ? !getElocationId().equals(article.getElocationId()) : article.getElocationId() != null)
            return false;
        if (getAbstractContent() != null ? !getAbstractContent().equals(article.getAbstractContent()) : article.getAbstractContent() != null)
            return false;
        return getLink() != null ? getLink().equals(article.getLink()) : article.getLink() == null;
    }
}
