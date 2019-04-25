package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Gene;

import java.io.IOException;
import java.util.*;

/**
 * Created by jiaojiao on 6/9/17.
 */
public class CancerGeneUtils {
    public static List<CancerGene> getCancerGeneList() {
        return CacheUtils.getCancerGeneList();
    }
}
