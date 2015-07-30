/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.Drug;
import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.EvidenceType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.TumorType;

/**
 *
 * @author jgao
 */
public interface EvidenceBo extends GenericBo<Evidence> {
    /**
     * Find Evidences by alteration ID
     * @param alterations
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations);
    
    /**
     * Find Evidences by alteration ID and evidence type
     * @param alterations
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes);

    /**
     * Find Evidences by alteration ID and tumor types
     * @param alterations
     * @param tumorTypes
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndTumorTypes(Collection<Alteration> alterations, Collection<TumorType> tumorTypes);

    /**
     * 
     * @param alterations
     * @param evidenceType
     * @param tumorTypes
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes);
    
    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return 
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes);
    
    /**
     * 
     * @param alterations
     * @return 
     */
    List<Drug> findDrugsByAlterations(Collection<Alteration> alterations);
}
