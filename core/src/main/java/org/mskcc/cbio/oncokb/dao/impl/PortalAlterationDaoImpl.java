/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.PortalAlterationDao;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;

/**
 *
 * @author jiaojiao
 */
public class PortalAlterationDaoImpl extends GenericDaoImpl<PortalAlteration, Integer> implements PortalAlterationDao {

    public List<PortalAlteration> findPortalAlterationCountByGene(Gene gene) {
        List<PortalAlteration> PortalAlteration = findByNamedQuery("findPortalAlterationCountByGene", gene);
        return PortalAlteration;
    }

    public List<PortalAlteration> findPortalAlterationCount() {
        List<PortalAlteration> PortalAlteration = findByNamedQuery("findPortalAlterationCount");
        return PortalAlteration;
    }
    
    public List<PortalAlteration> findMutationMapperData(Gene gene) {
        List<PortalAlteration> PortalAlteration = findByNamedQuery("findMutationMapperData", gene);
        return PortalAlteration;
    }
}
