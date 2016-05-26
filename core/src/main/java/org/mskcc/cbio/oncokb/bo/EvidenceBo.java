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
     * @param evidenceTypes
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes);

    /**
     * Find Evidences by alteration ID and evidence type
     * @param alterations
     * @param evidenceTypes
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlterationWithLevels(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<LevelOfEvidence> levelOfEvidences);


    /**
     * 
     * @param alterations
     * @param evidenceTypes
     * @param cancerTypes
     * @param type cancer types category: main or subtype
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<String> cancerTypes, String type);


    /**
     * 
     * @param alterations
     * @param evidenceTypes
     * @param cancerTypes
     * @param type cancer types category: main or subtype
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, Collection<String> cancerTypes, String type, Collection<LevelOfEvidence> levelOfEvidences);

    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return 
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes);

    /**
     * 
     * @param genes
     * @param evidenceTypes
     * @return
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes);

    /**
     * 
     * @param genes
     * @param evidenceTypes
     * @param cancerTypes
     * @param type cancer types category: main or subtype
     * @return
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<String> cancerTypes, String type);
    
    /**
     * 
     * @param alterations
     * @return 
     */
    List<Drug> findDrugsByAlterations(Collection<Alteration> alterations);

    /**
     *
     * @param alterations
     * @return
     */
    List<Evidence> findTumorTypesWithEvidencesForAlteration(List<Alteration> alterations);

    /**
     *
     * @param alterations
     * @return
     */
    List<Evidence> findCancerTypesWithEvidencesForAlteration(List<Alteration> alterations);

    /**
     *
     * @param alterations
     * @return
     */
    List<Evidence> findSubtypesWithEvidencesForAlteration(List<Alteration> alterations);
    
    List<String> findAllCancerTypes();
    
    List<String> findAllSubtypes();
}
