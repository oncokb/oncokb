package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class GeneUtils {
    public static Gene getGene(Integer entrezId, String hugoSymbol) {
        if (entrezId != null) {
            return getGeneByEntrezId(entrezId);
        } else if (hugoSymbol != null) {
            return getGeneByHugoSymbol(hugoSymbol);
        }
        return null;
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        if (hugoSymbol != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if (CacheUtils.isEnabled()) {
                if (CacheUtils.getGeneByHugoSymbol(hugoSymbol) == null) {
                    CacheUtils.setGeneByHugoSymbol(hugoSymbol, geneBo.findGeneByHugoSymbol(hugoSymbol));
                }
                return CacheUtils.getGeneByHugoSymbol(hugoSymbol);
            } else {
                return geneBo.findGeneByHugoSymbol(hugoSymbol);
            }
        }
        return null;
    }

    public static Gene getGeneByEntrezId(Integer entrezId) {
        if (entrezId != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if (CacheUtils.isEnabled()) {
                if (CacheUtils.getGeneByEntrezId(entrezId) == null) {
                    CacheUtils.setGeneByEntrezId(entrezId, geneBo.findGeneByEntrezGeneId(entrezId));
                }
                return CacheUtils.getGeneByEntrezId(entrezId);
            } else {
                geneBo.findGeneByEntrezGeneId(entrezId);
            }
        }
        return null;
    }

    public static Set<Gene> getAllGenes() {
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getAllGenes();
        } else {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            return new HashSet<>(geneBo.findAll());
        }
    }
}
