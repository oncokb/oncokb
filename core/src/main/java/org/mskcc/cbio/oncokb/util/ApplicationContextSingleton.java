/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author jgao
 */
public final class ApplicationContextSingleton {
    private final static ApplicationContext appContext = 
    		new ClassPathXmlApplicationContext("spring/config/BeanLocations.xml");
    
    public static void main(String[] args) {
        System.out.println(getGeneBo().findGeneByHugoSymbol("BRAF").getHugoSymbol());
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
    
    public static ClinicalTrialBo getClinicalTrialBo() {
        return ClinicalTrialBo.class.cast(getApplicationContext().getBean("clinicalTrialBo"));
    }
    
    public static NccnGuidelineBo getNccnGuidelineBo() {
        return NccnGuidelineBo.class.cast(getApplicationContext().getBean("nccnGuidelineBo"));
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
    
    public static TumorTypeBo getTumorTypeBo() {
        return TumorTypeBo.class.cast(getApplicationContext().getBean("tumorTypeBo"));
    }

    public static StatusBo getStatusBo() {
        return StatusBo.class.cast(getApplicationContext().getBean("statusBo"));
    }
}
