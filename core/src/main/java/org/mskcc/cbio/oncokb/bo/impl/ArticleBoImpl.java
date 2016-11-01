

package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.ArticleBo;
import org.mskcc.cbio.oncokb.dao.ArticleDao;
import org.mskcc.cbio.oncokb.model.Article;

/**
 *
 * @author jgao
 */
public class ArticleBoImpl extends GenericBoImpl<Article, ArticleDao> implements ArticleBo {

    @Override
    public Article findArticleByPmid(String pmid) {
        return getDao().findArticleByPmid(pmid);
    }
    @Override
    public Article findArticleByAbstract(String abstractContent) {
        return getDao().findArticleByAbstract(abstractContent);
    }
}
