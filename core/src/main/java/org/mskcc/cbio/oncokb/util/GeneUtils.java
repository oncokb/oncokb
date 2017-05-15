package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;

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

    // EntrezGeneId always has higher priority then HugoSymbol
    public static Gene getGene(Integer entrezGeneId, String hugoSymbol) {
        Gene gene = getGeneByEntrezId(entrezGeneId);
        if (gene != null) {
            return gene;
        }
        gene = getGeneByHugoSymbol(hugoSymbol);
        return gene;
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        if (hugoSymbol != null) {
            hugoSymbol = hugoSymbol.toUpperCase();
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if (CacheUtils.isEnabled()) {
                Gene gene = CacheUtils.getGeneByHugoSymbol(hugoSymbol);
                if (gene == null) {
                    gene = getGeneByAlias(hugoSymbol);
                }
                return gene;
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

    public static Gene getGeneByAlias(String geneAlias) {
        if (geneAlias != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if (CacheUtils.isEnabled()) {
                Set<Gene> genes = getAllGenes();
                Set<Gene> matches = new HashSet<>();
                for (Gene gene : genes) {
                    for (String alias : gene.getGeneAliases()) {
                        if (alias.equals(geneAlias)) {
                            matches.add(gene);
                            break;
                        }
                    }
                }
                if (matches.isEmpty() || matches.size() > 1) {
                    return null;
                } else {
                    return matches.iterator().next();
                }
            } else {
                return geneBo.findGeneByAlias(geneAlias);
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
                    } else {
                        for (String alias : gene.getGeneAliases()) {
                            if (StringUtils.containsIgnoreCase(alias, keywords)) {
                                genes.add(gene);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return genes;
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
