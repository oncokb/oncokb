package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Article;



/**
 *
 * @author jgao
 */
public interface ArticleDao extends GenericDao<Article, Integer> {
    /**
     * Get an Article by PMID
     * @param pmid
     * @return gene object or null
     */
    Article findArticleByPmid(String pmid);
    Article findArticleByAbstract(String abstractContent);
}
