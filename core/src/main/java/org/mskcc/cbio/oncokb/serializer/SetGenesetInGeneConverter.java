package org.mskcc.cbio.oncokb.serializer;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.mskcc.cbio.oncokb.model.Geneset;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Hongxin Zhang on 2019-08-13.
 */
public class SetGenesetInGeneConverter extends StdConverter<Set<Geneset>, Set<Geneset>> {
    @Override
    public Set<Geneset> convert(Set<Geneset> genesets) {
        Set<Geneset> newGenesets = new HashSet<>();
        for (Geneset geneset : genesets) {
            Geneset newGeneset = new Geneset();
            newGeneset.setId(geneset.getId());
            newGeneset.setUuid(geneset.getUuid());
            newGeneset.setName(geneset.getName());
            newGenesets.add(newGeneset);
        }
        return newGenesets;
    }
}
