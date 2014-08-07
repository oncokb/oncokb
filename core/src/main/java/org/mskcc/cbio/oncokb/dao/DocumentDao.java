package org.mskcc.cbio.oncokb.dao;

import org.mskcc.cbio.oncokb.model.Document;



/**
 *
 * @author jgao
 */
public interface DocumentDao extends GenericDao<Document, Integer> {
    /**
     * Get a Document by PMID
     * @param pmid
     * @return gene object or null
     */
    Document findDocumentbyPmid(String pmid);
}
