/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.importer;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.*;
import org.mskcc.cbio.oncokb.util.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnnotateAllVariants {
    public static void main(String[] args) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Alteration alteration : ApplicationContextSingleton.getAlterationBo().findAll()) {
            Query query = new Query();
            query.setHugoSymbol(alteration.getGene().getHugoSymbol());
            query.setAlteration(alteration.getAlteration());
            IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, true, null);
            if (indicatorQueryResp.getHighestSensitiveLevel() != null || indicatorQueryResp.getHighestResistanceLevel() != null) {
                List<String> content = new ArrayList<>();
                content.add(alteration.getGene().getHugoSymbol());
                content.add(alteration.getAlteration());
                content.add(alteration.getName());
                content.add(indicatorQueryResp.getHighestSensitiveLevel() == null ? "" : indicatorQueryResp.getHighestSensitiveLevel().toString());
                content.add(indicatorQueryResp.getHighestResistanceLevel() == null ? "" : indicatorQueryResp.getHighestResistanceLevel().toString());
                content.add(indicatorQueryResp.getMutationEffect() == null ? "" : indicatorQueryResp.getMutationEffect().getKnownEffect());
                content.add(indicatorQueryResp.getMutationEffect() == null ? "" : indicatorQueryResp.getMutationEffect().getDescription());
                lines.add(content.stream().collect(Collectors.joining("\t")));
            }
        }
        FileUtils.writeToLocal("all_actionable_variants_with_mutation_effect.tsv", lines.stream().collect(Collectors.joining("\n")));
    }
}
