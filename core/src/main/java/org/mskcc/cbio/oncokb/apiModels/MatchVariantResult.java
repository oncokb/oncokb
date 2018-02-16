package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Query;

import java.util.Set;

/**
 * Created by Hongxin Zhang on 2/13/18.
 */
public class MatchVariantResult implements java.io.Serializable {
    Query query;
    Set<MatchVariant> result;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Set<MatchVariant> getResult() {
        return result;
    }

    public void setResult(Set<MatchVariant> result) {
        this.result = result;
    }
}
