

package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.DocumentDao;
import org.mskcc.cbio.oncokb.model.Document;

/**
 *
 * @author jgao
 */
public class DocumentDaoImpl
            extends GenericDaoImpl<Document, Integer>
            implements DocumentDao  {
    /**
     * Get a Document by PMID
     * @param pmid
     * @return gene object or null
     */
    public Document findDocumentbyPmid(String pmid) {
        List<Document> list = findByNamedQuery("findDocumentbyPmid", pmid);
        return list.isEmpty() ? null : list.get(0);
    }
}
