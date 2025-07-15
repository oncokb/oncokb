package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;

import org.genome_nexus.client.GenomicLocation;
import org.mskcc.cbio.oncokb.dao.GenomicAlterationDao;
import org.mskcc.cbio.oncokb.model.GenomicAlteration;

public class GenomicAlterationDaoImpl extends GenericDaoImpl<GenomicAlteration, Integer> implements GenomicAlterationDao {

    @Override
    public GenomicAlteration findGenomicAlterationByGenomicLocation(GenomicLocation genomicLocation) {
        List<GenomicAlteration> genomicAlterations = findByNamedQuery(
            "findGenomicAlterationByGenomicLocation",
            genomicLocation.getChromosome(),
            genomicLocation.getStart(),
            genomicLocation.getEnd(),
            genomicLocation.getReferenceAllele(),
            genomicLocation.getVariantAllele()
        );
        return genomicAlterations.isEmpty() ? null : genomicAlterations.get(0);
    }}
