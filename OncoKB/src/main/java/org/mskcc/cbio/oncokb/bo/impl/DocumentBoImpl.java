

package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.bo.DocumentBo;
import org.mskcc.cbio.oncokb.dao.DocumentDao;
import org.mskcc.cbio.oncokb.model.Document;
import org.mskcc.cbio.oncokb.model.DocumentType;

/**
 *
 * @author jgao
 */
public class DocumentBoImpl extends GenericBoImpl<Document, DocumentDao> implements DocumentBo {
    
    /**
     * Find an existing doc
     * @param doc
     * @return 
     */
    public Document findDocument(Document doc) {
        if (doc.getDocType() == DocumentType.JOURNAL_ARTICLE) {
            if (doc.getPmid() == null) {
                return null;
            }
            return getDao().findDocumentbyPmid(doc.getPmid());
        }
        
        if (doc.getDocType() == DocumentType.CONFERENCE_ABSTRACT) {
            if (doc.getConference() == null || doc.getYear() == null || doc.getReference() == null) {
                return null;
            }
            List<Document> docs = getDao().findByParamValues(new String[]{"conference","year","reference"},
                    new String[]{doc.getConference(), doc.getYear(), doc.getReference()});
            if (docs.isEmpty()) {
                return null;
            }
            
            return docs.get(0);
        }
        
        if (doc.getDocType() == DocumentType.NCCN_GUIDELINES) {
            if (doc.getNccnDisease()== null || doc.getYear() == null || doc.getVersion()== null) {
                return null;
            }
            List<Document> docs = getDao().findByParamValues(new String[]{"nccn_disease","year","version"},
                    new String[]{doc.getNccnDisease(), doc.getYear(), doc.getVersion()});
            if (docs.isEmpty()) {
                return null;
            }
            
            return docs.get(0);
        }
        
        return null;
    }
}
