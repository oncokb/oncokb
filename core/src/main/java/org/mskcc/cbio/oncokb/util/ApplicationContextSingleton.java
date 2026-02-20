/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jgao
 */
public final class ApplicationContextSingleton {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextSingleton.class);

    private final static ApplicationContext appContext =
        new ClassPathXmlApplicationContext("spring/config/BeanLocations.xml");

    public static void main(String[] args) {
        LOGGER.info(getGeneBo().findGeneByHugoSymbol("BRAF").getHugoSymbol());
    }

    private static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public static AlterationBo getAlterationBo() {
        return AlterationBo.class.cast(getApplicationContext().getBean("alterationBo"));
    }

    public static ArticleBo getArticleBo() {
        return ArticleBo.class.cast(getApplicationContext().getBean("articleBo"));
    }

    public static EvidenceBo getEvidenceBo() {
        return EvidenceBo.class.cast(getApplicationContext().getBean("evidenceBo"));
    }

    public static TreatmentBo getTreatmentBo() {
        return TreatmentBo.class.cast(getApplicationContext().getBean("treatmentBo"));
    }

    public static GeneBo getGeneBo() {
        return GeneBo.class.cast(getApplicationContext().getBean("geneBo"));
    }

    public static VariantConsequenceBo getVariantConsequenceBo() {
        return VariantConsequenceBo.class.cast(getApplicationContext().getBean("variantConsequenceBo"));
    }

    public static DrugBo getDrugBo() {
        return DrugBo.class.cast(getApplicationContext().getBean("drugBo"));
    }

    public static PortalAlterationBo getPortalAlterationBo() {
        return PortalAlterationBo.class.cast(getApplicationContext().getBean("portalAlterationBo"));
    }

    public static GenesetBo getGenesetBo() {
        return GenesetBo.class.cast(getApplicationContext().getBean("genesetBo"));
    }

    public static TumorTypeBo getTumorTypeBo() {
        return TumorTypeBo.class.cast(getApplicationContext().getBean("tumorTypeBo"));
    }

    public static InfoBo getInfoBo() {
        return InfoBo.class.cast(getApplicationContext().getBean("infoBo"));
    }

    public static TagBo getTagBo() {
        return TagBo.class.cast(getApplicationContext().getBean("tagBo"));
    }
}
