

package org.mskcc.cbio.oncokb.model;

/**
 *
 * @author jgao
 */
public class Document implements java.io.Serializable {
    private Integer docId;
    private DocumentType docType;
    private String docTitle;
    private String docAbstract;
    private String reference;
    private String pmid;
    private String url;
    private String conference;
    private String year;
    private String pages;
    private String version;
    private String nccnDisease;
    private String comments;

    public Document() {
    }

    public Document(DocumentType documentType) {
        this.docType = documentType;
    }

    public Document(DocumentType docType, String docTitle, String docAbstract, String reference, String pmid, String url, String conference, String year, String pages, String version, String nccnDisease, String comments) {
        this.docType = docType;
        this.docTitle = docTitle;
        this.docAbstract = docAbstract;
        this.reference = reference;
        this.pmid = pmid;
        this.url = url;
        this.conference = conference;
        this.year = year;
        this.pages = pages;
        this.version = version;
        this.nccnDisease = nccnDisease;
        this.comments = comments;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public DocumentType getDocType() {
        return docType;
    }

    public void setDocType(DocumentType docType) {
        this.docType = docType;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDocAbstract() {
        return docAbstract;
    }

    public void setDocAbstract(String docAbstract) {
        this.docAbstract = docAbstract;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNccnDisease() {
        return nccnDisease;
    }

    public void setNccnDisease(String nccnDisease) {
        this.nccnDisease = nccnDisease;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.docType != null ? this.docType.hashCode() : 0);
        hash = 89 * hash + (this.docTitle != null ? this.docTitle.hashCode() : 0);
        hash = 89 * hash + (this.pmid != null ? this.pmid.hashCode() : 0);
        hash = 89 * hash + (this.conference != null ? this.conference.hashCode() : 0);
        hash = 89 * hash + (this.year != null ? this.year.hashCode() : 0);
        hash = 89 * hash + (this.pages != null ? this.pages.hashCode() : 0);
        hash = 89 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 89 * hash + (this.nccnDisease != null ? this.nccnDisease.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Document other = (Document) obj;
        if (this.docType != other.docType) {
            return false;
        }
        if ((this.docTitle == null) ? (other.docTitle != null) : !this.docTitle.equals(other.docTitle)) {
            return false;
        }
        if ((this.pmid == null) ? (other.pmid != null) : !this.pmid.equals(other.pmid)) {
            return false;
        }
        if ((this.conference == null) ? (other.conference != null) : !this.conference.equals(other.conference)) {
            return false;
        }
        if ((this.year == null) ? (other.year != null) : !this.year.equals(other.year)) {
            return false;
        }
        if ((this.pages == null) ? (other.pages != null) : !this.pages.equals(other.pages)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        if ((this.nccnDisease == null) ? (other.nccnDisease != null) : !this.nccnDisease.equals(other.nccnDisease)) {
            return false;
        }
        return true;
    }
    
    
}
