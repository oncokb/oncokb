/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mskcc.cbio.oncokb.apiModels.CancerTypeCount;
import org.mskcc.cbio.oncokb.dao.PortalAlterationDao;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;

/**
 * @author jiaojiao
 */
public class PortalAlterationDaoImpl extends GenericDaoImpl<PortalAlteration, Integer> implements PortalAlterationDao {

    public List<CancerTypeCount> findPortalAlterationCountByGene(Gene gene) {
        if (gene == null) return new ArrayList<>();
        List<Object[]> result = (List<Object[]>) getHibernateTemplate().findByNamedQuery("findPortalAlterationCountByGene", gene);
        return result.stream().map(datum -> new CancerTypeCount((String) datum[0], ((Long) datum[1]).intValue())).collect(Collectors.toList());
    }

    public List<CancerTypeCount> findPortalAlterationCount() {
        List<Object[]> result = (List<Object[]>) getHibernateTemplate().findByNamedQuery("findPortalAlterationCount");
        return result.stream().map(datum -> new CancerTypeCount((String) datum[0], ((Long) datum[1]).intValue())).collect(Collectors.toList());
    }

    public List<PortalAlteration> findMutationMapperData(Gene gene) {
        List<PortalAlteration> PortalAlteration = findByNamedQuery("findMutationMapperData", gene);
        return PortalAlteration;
    }
}
