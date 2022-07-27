package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.ArticleAbstract;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2/21/18.
 */
public class CurationPlatformConfigs {
    String curationLink;
    String apiLink;
    String privateApiLink;
    String internalPrivateApiLink;
    String publicApiLink;
    String internalPublicApiLink;
    String websocketApiLink;
    Boolean testing;
    Boolean production;
    FirebaseConfig firebaseConfig;

    public String getCurationLink() {
        return curationLink;
    }

    public void setCurationLink(String curationLink) {
        this.curationLink = curationLink;
    }

    public String getApiLink() {
        return apiLink;
    }

    public void setApiLink(String apiLink) {
        this.apiLink = apiLink;
    }

    public String getPrivateApiLink() {
        return privateApiLink;
    }

    public void setPrivateApiLink(String privateApiLink) {
        this.privateApiLink = privateApiLink;
    }

    public String getInternalPrivateApiLink() {
        return internalPrivateApiLink;
    }

    public void setInternalPrivateApiLink(String internalPrivateApiLink) {
        this.internalPrivateApiLink = internalPrivateApiLink;
    }

    public String getPublicApiLink() {
        return publicApiLink;
    }

    public void setPublicApiLink(String publicApiLink) {
        this.publicApiLink = publicApiLink;
    }

    public String getInternalPublicApiLink() {
        return internalPublicApiLink;
    }

    public void setInternalPublicApiLink(String internalPublicApiLink) {
        this.internalPublicApiLink = internalPublicApiLink;
    }

    public String getWebsocketApiLink() {
        return websocketApiLink;
    }

    public void setWebsocketApiLink(String websocketApiLink) {
        this.websocketApiLink = websocketApiLink;
    }

    public Boolean getTesting() {
        return testing;
    }

    public void setTesting(Boolean testing) {
        this.testing = testing;
    }

    public Boolean getProduction() {
        return production;
    }

    public void setProduction(Boolean production) {
        this.production = production;
    }

    public FirebaseConfig getFirebaseConfig() {
        return firebaseConfig;
    }

    public void setFirebaseConfig(FirebaseConfig firebaseConfig) {
        this.firebaseConfig = firebaseConfig;
    }
}
