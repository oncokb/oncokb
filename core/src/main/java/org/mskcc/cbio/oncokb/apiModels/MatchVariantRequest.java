package org.mskcc.cbio.oncokb.apiModels;

import org.mskcc.cbio.oncokb.model.Query;

import java.util.List;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2/13/18.
 */
public class MatchVariantRequest implements java.io.Serializable {
    Set<MatchVariant> oncokbVariants;
    List<Query> queries;

    public Set<MatchVariant> getOncokbVariants() {
        return oncokbVariants;
    }

    public void setOncokbVariants(Set<MatchVariant> oncokbVariants) {
        this.oncokbVariants = oncokbVariants;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }
}
