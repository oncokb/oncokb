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

    public static Set<Gene> searchGene(String keyword, Boolean exactSearch) {
        Set<Gene> genes = new HashSet<>();
        if (exactSearch == null)
            exactSearch = false;
        if (keyword != null && keyword != "") {
            Set<Gene> allGenes = GeneUtils.getAllGenes();
            if (org.apache.commons.lang3.math.NumberUtils.isNumber(keyword)) {
                for (Gene gene : allGenes) {
                    String entrezId = Integer.toString(gene.getEntrezGeneId());
                    if (exactSearch) {
                        if (entrezId.equals(keyword)) {
                            genes.add(gene);
                        }
                    } else {
                        if (entrezId.contains(keyword)) {
                            genes.add(gene);
                        }
                    }
                }
            } else {
                keyword = keyword.toLowerCase();
                for (Gene gene : allGenes) {
                    String hugoSymbol = gene.getHugoSymbol().toLowerCase();
                    if (exactSearch) {
                        if (hugoSymbol.equals(keyword)) {
                            genes.add(gene);
                        } else {
                            for (String alias : gene.getGeneAliases()) {
                                if (alias.toLowerCase().equals(keyword)) {
                                    genes.add(gene);
                                    break;
                                }
                            }
                        }
                    } else {
                        if (StringUtils.containsIgnoreCase(hugoSymbol, keyword)) {
                            genes.add(gene);
                        } else {
                            for (String alias : gene.getGeneAliases()) {
                                if (StringUtils.containsIgnoreCase(alias, keyword)) {
                                    genes.add(gene);
                                    break;
                                }
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

    public static Integer compareGenesByKeyword(Gene g1, Gene g2, String keyword) {
        if (g1 == null) {
            return 1;
        }
        if (g2 == null) {
            return -1;
        }
        String s1 = "";
        String s2 = "";
        Integer i1 = -1;
        Integer i2 = -1;

        if (StringUtils.isNumeric(keyword)) {
            s1 = Integer.toString(g1.getEntrezGeneId());
            s2 = Integer.toString(g2.getEntrezGeneId());
        } else {
            s1 = g1.getHugoSymbol().toLowerCase();
            s2 = g2.getHugoSymbol().toLowerCase();
        }
        if (s1.equals(keyword)) {
            return -1;
        }
        if (s2.equals(keyword)) {
            return 1;
        }

        i1 = s1.indexOf(keyword);
        i2 = s2.indexOf(keyword);

        if (i1.equals(i2) && i1.equals(-1)) {
            Integer i1Alias = 100;
            Integer i2Alias = 100;
            Integer index = -1;
            for (String geneAlias : g1.getGeneAliases()) {
                index = geneAlias.toLowerCase().indexOf(keyword);
                if (index > -1 && index < i1Alias) {
                    i1Alias = index;
                }
            }

            index = -1;
            for (String geneAlias : g2.getGeneAliases()) {
                index = geneAlias.toLowerCase().indexOf(keyword);
                if (index > -1 && index < i2Alias) {
                    i2Alias = index;
                }
            }
            if (i1Alias.equals(-1))
                return 1;
            if (i2Alias.equals(-1))
                return -1;
            return -1;
        } else {
            if (i1.equals(-1))
                return 1;
            if (i2.equals(-1))
                return -1;
            if (i1.equals(i2)) {
                Integer result = s1.compareTo(s2);
                if (result == 0) {
                    // Never returns 0, 0 may filter out record from list.
                    return -1;
                } else {
                    return result;
                }
            } else {
                return i1 - i2;
            }
        }
    }
}
