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
    
    private static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    public static AlterationBo getAlterationBo() {
        return AlterationBo.class.cast(getApplicationContext().getBean("alterationBo"));
    }
    
    public static DocumentBo getDocumentBo() {
        return DocumentBo.class.cast(getApplicationContext().getBean("documentBo"));
    }
    
    public static EvidenceBo getEvidenceBo() {
        return EvidenceBo.class.cast(getApplicationContext().getBean("evidenceBo"));
    }
    
    public static EvidenceBlobBo getEvidenceBlobBo() {
        return EvidenceBlobBo.class.cast(getApplicationContext().getBean("evidenceBlobBo"));
    }
    
    public static GeneBo getGeneBo() {
        return GeneBo.class.cast(getApplicationContext().getBean("geneBo"));
    }
    
    public static TumorTypeBo getTumorTypeBo() {
        return TumorTypeBo.class.cast(getApplicationContext().getBean("tumorTypeBo"));
    }
}
