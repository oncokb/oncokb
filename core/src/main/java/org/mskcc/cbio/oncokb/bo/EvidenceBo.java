/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;

import org.mskcc.cbio.oncokb.model.*;

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
     * Find Evidences by alteration ID and evidence type
     * @param alterations
     * @param evidenceType
     * @return
     */
    List<Evidence> findEvidencesByAlterationWithLevels(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<LevelOfEvidence> levelOfEvidences);

    /**
     * 
     * @param alterations
     * @param evidenceType
     * @param tumorTypes
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes);

    /**
     *
     * @param alterations
     * @param evidenceType
     * @param tumorTypes
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes, Collection<LevelOfEvidence> levelOfEvidences);

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
