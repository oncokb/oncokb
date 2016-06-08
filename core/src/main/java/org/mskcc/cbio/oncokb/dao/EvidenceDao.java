/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao;

import java.util.List;
import java.util.Map;

import org.mskcc.cbio.oncokb.model.*;

/**
 *
 * @author jgao
 */
public interface EvidenceDao extends GenericDao<Evidence, Integer> {
    
    /**
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Alteration alteration);

    /**
     * 
     * @param alteration
     * @param tumorType
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndTumorType(Alteration alteration, String tumorType);
    
    /**
     *
     * @param alteration
     * @param cancerType
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndCancerType(Alteration alteration, String cancerType);

    /**
     *
     * @param alteration
     * @param subtype
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndSubtype(Alteration alteration, String subtype);


    /**
     * 
     * @param alterations
     * @param tumorTypes
     * @param evidenceTypes
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypes(List<Alteration> alterations, List<String> tumorTypes, List<EvidenceType> evidenceTypes);
    
    /**
     *
     * @param alterations
     * @param cancerTypes
     * @param evidenceTypes
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypes(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes);
    
    /**
     *
     * @param alterations
     * @param subtypes
     * @param evidenceTypes
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndSubtypesAndEvidenceTypes(List<Alteration> alterations, List<String> subtypes, List<EvidenceType> evidenceTypes);

    /**
     * 
     * @param alterations
     * @param tumorTypes
     * @param evidenceTypes
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndTumorTypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> tumorTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences);
    
    /**
     * 
     * @param alterations
     * @param cancerTypes
     * @param evidenceTypes
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndCancerTypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> cancerTypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences);


    /**
     * 
     * @param alterations
     * @param subtypes
     * @param evidenceTypes
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlterationsAndSubtypesAndEvidenceTypesAndLevelOfEvidence(List<Alteration> alterations, List<String> subtypes, List<EvidenceType> evidenceTypes, List<LevelOfEvidence> levelOfEvidences);

    /**
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByAlteration(Alteration alteration, EvidenceType evidenceType);

    /**
     * Find AlterationActivityEvidences by alterations
     * @param alteration
     * @param evidenceType
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndLevels(Alteration alteration, EvidenceType evidenceType, LevelOfEvidence levelOfEvidence);

    /**
     * General searching between cancerType and subtype
     * @param alteration
     * @param evidenceType
     * @param tumorType
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndTumorType(Alteration alteration, EvidenceType evidenceType, String tumorType);
    /**
     * 
     * @param alteration
     * @param evidenceType
     * @param cancerType
     * @return 
     */
    List<Evidence> findEvidencesByAlterationAndCancerType(Alteration alteration, EvidenceType evidenceType, String cancerType);

    /**
     * 
     * @param alteration
     * @param evidenceType
     * @param subtype
     * @return
     */
    List<Evidence> findEvidencesByAlterationAndSubtype(Alteration alteration, EvidenceType evidenceType, String subtype);

    /**
     * Find AlterationActivityEvidences by Entrez Gene ID
     * @param gene
     * @return 
     */
    List<Evidence> findEvidencesByGene(Gene gene);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @return 
     */
    List<Evidence> findEvidencesByGene(Gene gene, EvidenceType evidenceType);

    /**
     * 
     * @param gene
     * @param evidenceType
     * @param tumorType
     * @return
     */
    List<Evidence> findEvidencesByGeneAndTumorType(Gene gene, EvidenceType evidenceType, String tumorType);
    
    /**
     * 
     * @param gene
     * @param evidenceType
     * @param cancerType
     * @return 
     */
    List<Evidence> findEvidencesByGeneAndCancerType(Gene gene, EvidenceType evidenceType, String cancerType);

    /**
     *
     * @param gene
     * @param evidenceType
     * @param subtype
     * @return
     */
    List<Evidence> findEvidencesByGeneAndSubtype(Gene gene, EvidenceType evidenceType, String subtype);

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
