

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.ArticleDao;
import org.mskcc.cbio.oncokb.model.Article;

/**
 *
 * @author jgao
 */
public class ArticleDaoImpl
            extends GenericDaoImpl<Article, Integer>
            implements ArticleDao  {
    /**
     * Get an Article by PMID
     * @param pmid
     * @return gene object or null
     */
    public Article findArticleByPmid(String pmid) {
        List<Article> list = findByNamedQuery("findArticleByPmid", pmid);
        return list.isEmpty() ? null : list.get(0);
    }
    
    public Article findArticleByAbstract(String abstractContent) {
        List<Article> list = findByNamedQuery("findArticleByAbstract", abstractContent);
        return list.isEmpty() ? null : list.get(0);
    }
}
