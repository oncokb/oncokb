
package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.Document;

/**
 *
 * @author jgao
 */
public interface DocumentBo extends GenericBo<Document> {
    /**
     * Find an existing doc
     * @param doc
     * @return 
     */
    Document findDocument(Document doc);
    
    /**
     * 
     * @param pmid
     * @return 
     */
    Document findDocumentByPmid(String pmid);
}
