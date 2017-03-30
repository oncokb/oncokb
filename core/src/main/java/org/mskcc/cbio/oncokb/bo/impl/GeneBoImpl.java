/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo.impl;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.dao.GeneDao;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jgao
 */
public class GeneBoImpl extends GenericBoImpl<Gene, GeneDao> implements GeneBo {
    @Override
    public Gene findGeneByHugoSymbol(String symbol) {
        return getDao().findGeneByHugoSymbol(symbol);
    }

    @Override
    public List<Gene> findGenesByHugoSymbol(Collection<String> symbols) {
        List<Gene> genes = new ArrayList<Gene>();
        for (String symbol : symbols) {
            Gene gene = getDao().findGeneByHugoSymbol(symbol);
            if (gene != null) {
                genes.add(gene);
            }
        }
        return genes;
    }

    @Override
    public Gene findGeneByEntrezGeneId(int entrezGeneId) {
        return getDao().findGeneByEntrezGeneId(entrezGeneId);
    }

    @Override
    public List<Gene> findGenesByEntrezGeneId(Collection<Integer> entrezGeneIds) {
        List<Gene> genes = new ArrayList<Gene>();
        for (int entrezGeneId : entrezGeneIds) {
            Gene gene = getDao().findGeneByEntrezGeneId(entrezGeneId);
            if (gene != null) {
                genes.add(gene);
            }
        }
        return genes;
    }

    @Override
    public Gene findGeneByAlias(String geneAlias) {
        return getDao().findGeneByAlias(geneAlias);
    }
}
