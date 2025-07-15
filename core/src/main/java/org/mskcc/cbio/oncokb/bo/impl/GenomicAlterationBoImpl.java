package org.mskcc.cbio.oncokb.bo.impl;

import org.genome_nexus.client.GenomicLocation;
import org.mskcc.cbio.oncokb.dao.GenomicAlterationDao;
import org.mskcc.cbio.oncokb.model.GenomicAlteration;

public class GenomicAlterationBoImpl extends GenericBoImpl<GenomicAlteration, GenomicAlterationDao> implements GenomicAlterationBo {

    @Override
    public GenomicAlteration findGenomicAlterationByGenomicLocation(GenomicLocation genomicLocation) {
        return getDao().findGenomicAlterationByGenomicLocation(genomicLocation);
    }
}
