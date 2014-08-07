/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.AlterationDao;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;

/**
 *
 * @author jgao
 */
public class AlterationDaoImpl extends GenericDaoImpl<Alteration, Integer> implements AlterationDao {
    @Override
    public List<Alteration> findAlterationsByGene(Gene gene) {
        return findByNamedQuery("findAlterationsByGene", gene.getEntrezGeneId());
    }
    
    @Override
    public Alteration findAlteration(Gene gene, AlterationType alterationType, String alteration) {
        List<Alteration> alterations = findByNamedQuery("findAlteration", gene.getEntrezGeneId(), alterationType, alteration);
        return alterations.isEmpty() ? null : alterations.get(0);
    }
}
