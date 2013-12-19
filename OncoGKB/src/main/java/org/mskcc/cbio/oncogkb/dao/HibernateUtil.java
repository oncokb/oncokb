/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao;

import java.util.Collection;
import java.util.Collections;
import org.hibernate.Session;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author jgao
 */
public final class HibernateUtil {
    
    private HibernateUtil() {
	throw new AssertionError();
    }

    private static final SessionFactory sessionFactory;
    
    static {
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception. 
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    /**
     * delete from db
     * @param obj 
     */
    public static void delete(Object obj) {
        delete(Collections.singletonList(obj));
    }
    
    /**
     * Delete from db.
     * @param objs 
     */
    public static void delete(Collection<Object> objs) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        for (Object obj : objs) {
            session.delete(obj);
        }
        session.getTransaction().commit();
    }
}
