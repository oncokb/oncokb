package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ShortGene;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class GeneUtils {
    public static Gene getGene(String gene) {
        if (StringUtils.isNumeric(gene)) {
            return getGeneByEntrezId(Integer.parseInt(gene));
        } else {
            return getGeneByHugoSymbol(gene);
        }
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        if (hugoSymbol != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if (CacheUtils.isEnabled()) {
                if (CacheUtils.getGeneByHugoSymbol(hugoSymbol) == null) {
                    CacheUtils.setGeneByHugoSymbol(geneBo.findGeneByHugoSymbol(hugoSymbol));
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
                    CacheUtils.setGeneByEntrezId(geneBo.findGeneByEntrezGeneId(entrezId));
                }
                return CacheUtils.getGeneByEntrezId(entrezId);
            } else {
                return geneBo.findGeneByEntrezGeneId(entrezId);
            }
        }
        return null;
    }

    public static Set<Gene> searchGene(String keywords) {
        Set<Gene> genes = new HashSet<>();
        if (keywords != null && keywords != "") {
            Set<Gene> allGenes = GeneUtils.getAllGenes();
            if (org.apache.commons.lang3.math.NumberUtils.isNumber(keywords)) {
                for (Gene gene : allGenes) {
                    String entrezId = Integer.toString(gene.getEntrezGeneId());
                    if (entrezId.contains(keywords)) {
                        genes.add(gene);
                    }
                }
            } else {
                for (Gene gene : allGenes) {
                    String hugoSymbol = gene.getHugoSymbol();
                    if (StringUtils.containsIgnoreCase(hugoSymbol, keywords)) {
                        genes.add(gene);
                    }
                }
            }
        }

        return genes;
    }

    public static Set<ShortGene> searchShortGene(String keywords) {
        Set<ShortGene> shortGenes = new HashSet<>();
        Set<Gene> genes = searchGene(keywords);

        for (Gene gene : genes) {
            shortGenes.add(convertToShort(gene));
        }
        return shortGenes;
    }

    public static ShortGene convertToShort(Gene gene) {
        ShortGene shortGene = null;
        if (gene != null) {
            shortGene = new ShortGene();
            shortGene.setEntrezGeneId(gene.getEntrezGeneId());
            shortGene.setHugoSymbol(gene.getHugoSymbol());
            shortGene.setGeneAliases(gene.getGeneAliases());
        }
        return shortGene;
    }

    public static Set<Gene> getAllGenes() {
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getAllGenes();
        } else {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            return new HashSet<>(geneBo.findAll());
        }
    }

    public static Boolean isSameGene(Integer entrezGeneId, String hugoSymbol) {
        Boolean flag = false;
        Gene entrezGene = null;
        Gene hugoGene = null;
        if (entrezGeneId != null) {
            entrezGene = GeneUtils.getGeneByEntrezId(entrezGeneId);
        }

        if (hugoSymbol != null) {
            hugoGene = GeneUtils.getGeneByHugoSymbol(hugoSymbol);
        }

        if (entrezGene != null && hugoGene != null && entrezGene.equals(hugoGene)) {
            flag = true;
        }
        return flag;
    }
}
