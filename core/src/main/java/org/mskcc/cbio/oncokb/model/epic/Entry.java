package org.mskcc.cbio.oncokb.model.epic;

import java.util.ArrayList;

public class Entry{

    private ArrayList<Link> link;
    private String fullUrl;
    private Resource resource;
    private Search search;

    public ArrayList<Link> getLink() {
        return link;
    }

    public void setLink(ArrayList<Link> link) {
        this.link = link;
    }
    
    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Search getSearch() {
        return search;
    }
    
    public void setSearch(Search search) {
        this.search = search;
    }
}
