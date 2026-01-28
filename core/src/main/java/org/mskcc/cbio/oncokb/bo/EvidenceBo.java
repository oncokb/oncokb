/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.bo;

import java.util.Collection;
import java.util.List;

import org.mskcc.cbio.oncokb.model.*;;

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
     * @param tumorType
     * @param tumorTypes
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, TumorType tumorType, List<TumorType> tumorTypes);


    /**
     *
     * @param alterations
     * @param evidenceTypes
     * @param tumorType
     * @param levelOfEvidences
     * @return
     */
    List<Evidence> findEvidencesByAlteration(Collection<Alteration> alterations, Collection<EvidenceType> evidenceTypes, TumorType tumorType, List<TumorType> tumorTypes, Collection<LevelOfEvidence> levelOfEvidences);

    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes);


    /**
     * Find Evidences by Entrez Gene ID
     * @param genes
     * @return
     */
    List<Evidence> findEvidencesByGeneFromDB(Collection<Gene> genes);

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
     * @param tumorTypes
     * @return
     */
    List<Evidence> findEvidencesByGene(Collection<Gene> genes, Collection<EvidenceType> evidenceTypes, Collection<TumorType> tumorTypes);

    /**
     *
     * @param ids
     * @return
     */
    List<Evidence> findEvidencesByIds(List<Integer> ids);
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
    List<Object> findTumorTypesWithEvidencesForAlterations(List<Alteration> alterations);

    /**
     *
     * @param alterations
     * @return
     */
    List<Object> findCancerTypesWithEvidencesForAlterations(List<Alteration> alterations);

    /**
     *
     * @param alterations
     * @return
     */
    List<Object> findSubtypesWithEvidencesForAlterations(List<Alteration> alterations);

    List<Evidence> findEvidenceByUUIDs(List<String> uuids);

    List<Evidence> findEvidenceByTagCriteria(
        int entrezGeneId,
        int start, 
        int end, 
        Oncogenicity oncogenicity,
        MutationType mutationType,
        List<EvidenceType> evidenceTypes
    );
}
