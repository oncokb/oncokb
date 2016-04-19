/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.bo.PortalAlterationBo;
import org.mskcc.cbio.oncokb.dao.PortalAlterationDao;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;

/**
 *
 * @author jiaojiao
 */
public class PortalAlterationBoImpl extends GenericBoImpl<PortalAlteration, PortalAlterationDao> implements PortalAlterationBo {

    @Override
    public List<PortalAlteration> findPortalAlterationCountByGene(Gene gene) {
        return getDao().findPortalAlterationCountByGene(gene);
    }

    @Override
    public List<PortalAlteration> findPortalAlterationCount() {
        return getDao().findPortalAlterationCount();
    }
}
