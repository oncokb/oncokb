package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.Treatment;

import java.util.*;


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
        if (entrezGeneId != null && !entrezGeneId.equals(0)) {
            return getGeneByEntrezId(entrezGeneId);
        } else {
            return getGeneByHugoSymbol(hugoSymbol);
        }
    }

    public static Gene getGeneByHugoSymbol(String hugoSymbol) {
        if (hugoSymbol != null) {
            Gene gene = CacheUtils.getGeneByHugoSymbol(hugoSymbol);
            if (gene == null) {
                gene = getGeneByAlias(hugoSymbol);
            }
            return gene;
        }
        return null;
    }

    public static Gene getGeneByEntrezId(Integer entrezId) {
        if (entrezId != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            return CacheUtils.getGeneByEntrezId(entrezId);
        }
        return null;
    }

    public static Gene getGeneByAlias(String geneAlias) {
        if (geneAlias != null) {
            Set<Gene> genes = CacheUtils.getAllGenes();
            Set<Gene> matches = new HashSet<>();
            for (Gene gene : genes) {
                if (gene.getGeneAliases().contains(geneAlias)) {
                    matches.add(gene);
                }
                if (matches.size() > 0) {
                    break;
                }
            }
            if (matches.isEmpty() || matches.size() > 1) {
                return null;
            } else {
                return matches.iterator().next();
            }
        }
        return null;
    }

    public static LinkedHashSet<Gene> searchGene(String keyword, Boolean exactSearch) {
        LinkedHashSet<Gene> genes = new LinkedHashSet<>();
        if (exactSearch == null)
            exactSearch = false;
        if (keyword != null && keyword != "") {
            Set<Gene> allGenes = CacheUtils.getAllGenes();
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
                    String hugoSymbol = gene.getHugoSymbol();
                    if (exactSearch) {
                        if (hugoSymbol.equalsIgnoreCase(keyword)) {
                            genes.add(gene);
                        }
                    } else {
                        if (StringUtils.containsIgnoreCase(hugoSymbol, keyword)) {
                            genes.add(gene);
                        }
                    }
                }
                for (Gene gene : allGenes) {
                    if (exactSearch) {
                        for (String alias : gene.getGeneAliases()) {
                            if (alias.toLowerCase().equals(keyword)) {
                                genes.add(gene);
                                break;
                            }
                        }
                    } else {
                        for (String alias : gene.getGeneAliases()) {
                            if (StringUtils.containsIgnoreCase(alias, keyword)) {
                                genes.add(gene);
                                break;
                            }
                        }
                    }
                }

                // If the keyword contains dash and exact search is false, then we should return both fusion genes
                if (keyword.contains("-") && exactSearch == false) {
                    for (String subKeyword : keyword.split("-")) {
                        genes.addAll(searchGene(subKeyword, false));
                    }
                }
            }
        }

        return genes;
    }

    public static Set<Gene> getGenesWithDrug(Drug drug) {
        Set<Gene> relatedGenes = new HashSet<>();
        if (drug == null)
            return relatedGenes;
        for (Evidence evidence : CacheUtils.getAllEvidences()) {
            for (Treatment treatment : evidence.getTreatments()) {
                if (treatment.getDrugs().contains(drug)) {
                    relatedGenes.add(evidence.getGene());
                    break;
                }
            }
        }
        return relatedGenes;
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
            if (i1Alias.equals(i2Alias))
                return -1;
            return i1Alias - i2Alias;
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
