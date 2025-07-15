package org.mskcc.cbio.oncokb.bo.impl;

import org.genome_nexus.client.GenomicLocation;
import org.mskcc.cbio.oncokb.bo.GenericBo;
import org.mskcc.cbio.oncokb.model.GenomicAlteration;

public interface GenomicAlterationBo extends GenericBo<GenomicAlteration> {
    GenomicAlteration findGenomicAlterationByGenomicLocation(GenomicLocation genomicLocation);
}
