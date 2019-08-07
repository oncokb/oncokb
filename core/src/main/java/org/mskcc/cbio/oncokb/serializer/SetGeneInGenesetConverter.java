package org.mskcc.cbio.oncokb.serializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.mskcc.cbio.oncokb.model.Gene;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-08-13.
 */
public class SetGeneInGenesetConverter extends StdConverter<Set<Gene>, Set<Gene>> {
    @Override
    public Set<Gene> convert(Set<Gene> genes) {
        Set<Gene> newGenes = new HashSet<>();
        for (Gene gene : genes) {
            Gene newGene = new Gene();
            newGene.setGeneAliases(newGene.getGeneAliases());
            newGene.setHugoSymbol(gene.getHugoSymbol());
            newGene.setEntrezGeneId(gene.getEntrezGeneId());
            newGene.setOncogene(gene.getOncogene());
            newGene.setTSG(gene.getTSG());
            newGene.setName(gene.getName());
            newGene.setCuratedIsoform(gene.getCuratedIsoform());
            newGene.setCuratedRefSeq(gene.getCuratedRefSeq());
            newGenes.add(newGene);
        }
        return newGenes;
    }
}
