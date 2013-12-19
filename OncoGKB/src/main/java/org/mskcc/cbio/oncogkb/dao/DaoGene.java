/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mskcc.cbio.oncogkb.model.Gene;
import org.mskcc.cbio.oncogkb.model.GeneAlias;
import org.mskcc.cbio.oncogkb.model.GeneLabel;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public final class DaoGene {
    
    private DaoGene() {
        throw new AssertionError();
    }
    
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    public static Gene getGeneByHugoSymbol(String symbol) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery ("from Gene as gene where gene.hugoSymbol=?");
        q.setString(0, symbol);
        List list = q.list();
        tx.commit();
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    public static Gene getGeneByEntrezGeneId(int entrezGeneId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery ("from Gene as gene where gene.entrezGeneId=?");
        q.setInteger(0, entrezGeneId);
        List list = q.list();
        tx.commit();
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Get GeneAlias 
     * @param entrezGeneId
     * @param alias
     * @return 
     */
    public static GeneAlias getGeneAlias(int entrezGeneId, String alias) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery ("from GeneAlias as gene_alias"
                + " where gene_alias.gene=?"
                + " and gene_alias.alias=?");
        q.setInteger(0, entrezGeneId);
        q.setString(1, alias);
        List list = q.list();
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return GeneAlias.class.cast(list.get(0));
    }
    
    /**
     * get GeneLabel
     * @param entrezGeneId
     * @param label
     * @return 
     */
    public static GeneLabel getGeneLabel(int entrezGeneId, String label) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery ("from GeneLabel as gene_label"
                + " where gene_label.gene=?"
                + " and gene_label.label=?");
        q.setInteger(0, entrezGeneId);
        q.setString(1, label);
        List list = q.list();
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return GeneLabel.class.cast(list.get(0));
    }
    
    /**
     * Save a gene to db.
     * @param gene 
     */
    public static void saveGene(Gene gene) {
        saveGenes(Collections.singletonList(gene));
    }
    
    /**
     * Save genes to db.
     * @param genes 
     */
    public static void saveGenes(Collection<Gene> genes) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        for (Gene gene : genes) {
            session.saveOrUpdate(gene);
            
            Set<GeneAlias> aliases = gene.getGeneAliases();
            if (aliases!=null) {
                for (GeneAlias alias : aliases) {
                    session.saveOrUpdate(alias);
                }
            }
            
            Set<GeneLabel> labels = gene.getGeneLabels();
            if (labels!=null) {
                for (GeneLabel label : labels) {
                    session.saveOrUpdate(label);
                }
            }
        }
        
        tx.commit();
    }
}
