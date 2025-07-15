package org.mskcc.cbio.oncokb.dao;

import org.genome_nexus.client.GenomicLocation;
import org.mskcc.cbio.oncokb.model.GenomicAlteration;

public interface GenomicAlterationDao extends GenericDao<GenomicAlteration, Integer> {
    GenomicAlteration findGenomicAlterationByGenomicLocation(GenomicLocation genomicLocation);
}
