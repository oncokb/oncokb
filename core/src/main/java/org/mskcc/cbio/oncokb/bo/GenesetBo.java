package org.mskcc.cbio.oncokb.bo;

import org.mskcc.cbio.oncokb.model.*;

/**
 * @author Hongxin Zhang
 */
public interface GenesetBo extends GenericBo<Geneset> {
    Geneset findGenesetByUuid(String uuid);
    Geneset findGenesetByName(String name);
}
