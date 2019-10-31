/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.List;

import org.mskcc.cbio.oncokb.apiModels.CancerTypeCount;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.PortalAlteration;

/**
 *
 * @author jiaojiao
 */
public interface PortalAlterationBo extends GenericBo<PortalAlteration> {

    List<CancerTypeCount> findPortalAlterationCountByGene(Gene gene);

    List<CancerTypeCount> findPortalAlterationCount();

    List<PortalAlteration> findMutationMapperData(Gene gene);
}
