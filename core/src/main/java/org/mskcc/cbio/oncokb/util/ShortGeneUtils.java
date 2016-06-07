package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ShortGene;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hongxinzhang on 4/5/16.
 */
public class ShortGeneUtils {
    public static ShortGene getShortGeneFromGene(Gene gene) {
        if (gene != null) {
            ShortGene shortGene = new ShortGene();
            shortGene.setEntrezGeneId(gene.getEntrezGeneId());
            shortGene.setHugoSymbol(gene.getHugoSymbol());
            shortGene.setName(gene.getName());
            shortGene.setGeneAliases(gene.getGeneAliases());
            shortGene.setGeneLabels(gene.getGeneLabels());
            return shortGene;
        } else {
            return null;
        }
    }

    public static Set<ShortGene> getAllShortGenes() {
        if (CacheUtils.isEnabled()) {
            return CacheUtils.getAllShortGenes();
        } else {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            return getShortGenesFromGenes(new HashSet<Gene>(geneBo.findAll()));
        }
    }

    public static Set<ShortGene> getShortGenesFromGenes(Set<Gene> genes) {
        Set<ShortGene> shortGenes = new HashSet<>();
        for (Gene gene : genes) {
            shortGenes.add(getShortGeneFromGene(gene));
        }
        return shortGenes;
    }
}
