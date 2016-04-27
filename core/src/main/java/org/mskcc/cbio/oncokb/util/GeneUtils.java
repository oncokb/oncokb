package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class GeneUtils {
    public static Boolean hasGene(Integer entrezId, String hugoSymbol) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;

        if (entrezId != null) {
            gene = geneBo.findGeneByEntrezGeneId(entrezId);
        } else if (hugoSymbol != null) {
            gene = geneBo.findGeneByHugoSymbol(hugoSymbol);
        }

        return gene == null ? false : true;
    }

    public static Gene getGene(Integer entrezId, String hugoSymbol) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        if (entrezId != null) {
            if(CacheUtils.isEnabled()) {
                if (CacheUtils.getGeneByEntrezId(entrezId) == null) {
                    CacheUtils.setGeneByEntrezId(entrezId, geneBo.findGeneByEntrezGeneId(entrezId));
                }
                return CacheUtils.getGeneByEntrezId(entrezId);
            }else {
                geneBo.findGeneByEntrezGeneId(entrezId);
            }
        } else if (hugoSymbol != null) {
            if(CacheUtils.isEnabled()) {
                if (CacheUtils.getGeneByHugoSymbol(hugoSymbol) == null) {
                    CacheUtils.setGeneByHugoSymbol(hugoSymbol, geneBo.findGeneByHugoSymbol(hugoSymbol));
                }
                return CacheUtils.getGeneByHugoSymbol(hugoSymbol);
            }else {
                return geneBo.findGeneByHugoSymbol(hugoSymbol);
            }
        }
        return null;
    }
}
