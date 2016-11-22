
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.Article;

/**
 *
 * @author jgao
 */
public interface ArticleBo extends GenericBo<Article> {
    
    /**
     * 
     * @param pmid
     * @return 
     */
    Article findArticleByPmid(String pmid);
    Article findArticleByAbstract(String abstractContent);
}
