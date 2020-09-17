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
            newGene.setGrch37Isoform(gene.getGrch37Isoform());
            newGene.setGrch37RefSeq(gene.getGrch37RefSeq());
            newGene.setGrch38Isoform(gene.getGrch38Isoform());
            newGene.setGrch38RefSeq(gene.getGrch38RefSeq());
            newGenes.add(newGene);
        }
        return newGenes;
    }
}
