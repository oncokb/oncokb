/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;

/**
 *
 * @author jgao
 */
public class AlterationDaoImpl extends GenericDaoImpl<Alteration, Integer> implements AlterationDao {

    public List<Alteration> findAlterationsByGene(int entrezGeneId) {
        return findByNamedQuery("findAlterationsByGene", entrezGeneId);
    }
    
    public Alteration findAlteration(int entrezGeneId, String alteration) {
        List<Alteration> alterations = findByNamedQuery("findAlteration", entrezGeneId, alteration);
        return alterations.isEmpty() ? null : alterations.get(0);
    }
}
