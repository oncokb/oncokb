package org.mskcc.cbio.oncokb.model.epic;

import static org.mskcc.cbio.oncokb.util.AlterationUtils.getAlterationFromGenomeNexus;
import static org.mskcc.cbio.oncokb.util.AlterationUtils.parseMutationString;

import java.util.ArrayList;
import java.util.List;

import org.genome_nexus.ApiException;
import org.mskcc.cbio.oncokb.EpicConstants;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateCopyNumberAlterationQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateMutationByProteinChangeQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateSampleQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.AnnotateStructuralVariantQuery;
import org.mskcc.cbio.oncokb.apiModels.annotation.QueryGene;
import org.mskcc.cbio.oncokb.genomenexus.GNVariantAnnotationType;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.CopyNumberAlterationType;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.StructuralVariantType;

public class GenomicData {

    private String resourceType;
    private String type;
    private int total;
    private ArrayList<Link> link;
    private ArrayList<Entry> entry;
    
    public List<AnnotateSampleQuery> getSamples() {
        List<List<String>> entryGroups = new ArrayList<>();
        List<Entry> allEntries = new ArrayList<>();
        for (Entry e : entry) {
            Resource resource = e.getResource();

            List<String> entryGroup = new ArrayList<>();
            if (resource.getCode().getCoding().get(0).getCode().equals(EpicConstants.GROUPER_CODE)) {
                for (HasMember member : resource.getHasMember()) {
                    entryGroup.add(member.getReference());
                }
            }

            entryGroups.add(entryGroup);
            allEntries.add(e);
        }

        List<AnnotateSampleQuery> samples = new ArrayList<>();
        for (int i = 0; i < entryGroups.size(); i++) {
            AnnotateSampleQuery sample = new AnnotateSampleQuery();
            samples.add(sample);

            for (Entry e : allEntries) {
                for (String entryId : entryGroups.get(i)) {
                    if (e.getFullUrl().contains(entryId)) {
                        addEntryToSample(sample, e);
                    }
                }
            }
        }
        return samples;
    }

    private void addEntryToSample(AnnotateSampleQuery sample, Entry e) {
        // Common properties
        VariantType variantType = VariantType.SIMPLE;
        List<String> hugoSymbols = new ArrayList<>();
        ReferenceGenome referenceGenome = null;

        // Mutations
        String proteinChange = null;
        String hgvsg = null;

        // Structural variants
        StructuralVariantType structuralVariantType = null;

        // Copy number alterations
        CopyNumberAlterationType copyNumberAlterationType = null;

        ArrayList<Extension> extensions = e.getResource().getExtension();
        if (extensions != null) {
            for (Extension extension : extensions) {
                if (extension.getUrl().contains("variant-type")) {
                    String display = extension.getValueCodeableConcept().getCoding().get(0).getDisplay();
                    if (display.equals(VariantType.SIMPLE.label)) {
                        variantType = VariantType.SIMPLE;
                    } else if (display.equals(VariantType.STRUCTURAL.label)) {
                        variantType = VariantType.STRUCTURAL;
                    } else if (display.equals(VariantType.COPY_NUMBER_ALTERATION.label)) {
                        variantType = VariantType.COPY_NUMBER_ALTERATION;
                    } else {
                        return;
                    }
                }
            }
        }

        for (Component component : e.getResource().getComponent()) {
            switch (component.getCode().getCoding().get(0).getCode()) {
                case EpicConstants.GENE_STUDIED_CODE:
                    hugoSymbols.add(component.getValueCodeableConcept().getText());
                    break;
                case EpicConstants.DNA_CHG_TYPE_CODE: // c.
                    if (variantType == VariantType.STRUCTURAL) {
                        structuralVariantType = parseDnaChangeTypeStructural(component.getValueCodeableConcept().getCoding().get(0).getDisplay());
                    } else if (variantType == VariantType.COPY_NUMBER_ALTERATION) {
                        copyNumberAlterationType = parseDnaChangeTypeCopyNumber(component.getValueCodeableConcept().getCoding().get(0).getDisplay());
                    }
                    break;
                case EpicConstants.AMINO_ACID_CHG_CODE:  // p.
                    proteinChange = component.getValueCodeableConcept().getText();
                    break;
                case EpicConstants.GENOMIC_DNA_CHG_CODE: // g.
                    hgvsg = component.getValueCodeableConcept().getText();
                    break;
                case EpicConstants.REFERENCE_SEQUENCE_ASSEMBLY_CODE:
                    for (ReferenceGenome genome : ReferenceGenome.values()) {
                        if (genome.name().toLowerCase().equals(component.getValueCodeableConcept().getCoding().get(0).getDisplay())) {
                            referenceGenome = genome;
                            break;
                        }
                    }
                default:
                    break;
            }
        }

        Alteration alteration = null;
        if (proteinChange != null) {
            List<Alteration> alterations = parseMutationString(proteinChange, "$");
            if (alterations.size() > 0) {
                alteration = alterations.get(0);
            }
        } else if (hgvsg != null) {
            try {
                alteration = getAlterationFromGenomeNexus(GNVariantAnnotationType.HGVS_G, referenceGenome, hgvsg);
            } catch (ApiException exception) {}
        }

        switch (variantType) {
            case SIMPLE:
                if (alteration == null) {
                    // currently we return, but we can try to construct the alteration with the available info
                    return;
                }

                AnnotateMutationByProteinChangeQuery newMutation = new AnnotateMutationByProteinChangeQuery();
                if (hugoSymbols.size() > 0) {
                    newMutation.setGene(new QueryGene(null, hugoSymbols.get(0)));
                }
                newMutation.setAlteration(alteration.getAlteration());
                newMutation.setReferenceGenome(referenceGenome);
                newMutation.setConsequence(alteration.getConsequence().getTerm());
                newMutation.setProteinStart(alteration.getProteinStart());
                newMutation.setProteinEnd(alteration.getProteinEnd());
                // newMutation.setTumorType(alteration.get);
                newMutation.setEvidenceTypes(newMutation.getEvidenceTypes());

                sample.getMutations().getProteinChange().add(newMutation);
                break;
            case STRUCTURAL:
                if (structuralVariantType == null) {
                    return;
                }

                AnnotateStructuralVariantQuery newStructuralVariant = new AnnotateStructuralVariantQuery();
                newStructuralVariant.setStructuralVariantType(structuralVariantType);
                if (hugoSymbols.size() > 0) {
                    newStructuralVariant.setGeneA(new QueryGene(null, hugoSymbols.get(0)));
                }
                if (hugoSymbols.size() > 1) {
                    newStructuralVariant.setGeneB(new QueryGene(null, hugoSymbols.get(1)));
                }
                if (referenceGenome != null) {
                    newStructuralVariant.setReferenceGenome(referenceGenome);
                }

                sample.getStructuralVariants().add(newStructuralVariant);
                break;
            case COPY_NUMBER_ALTERATION:
                if (copyNumberAlterationType == null) {
                    return;
                }

                AnnotateCopyNumberAlterationQuery newCopyNumberAlteration = new AnnotateCopyNumberAlterationQuery();
                newCopyNumberAlteration.setCopyNameAlterationType(copyNumberAlterationType);
                if (hugoSymbols.size() > 0) {
                    newCopyNumberAlteration.setGene(new QueryGene(null, hugoSymbols.get(0)));
                }
                if (referenceGenome != null) {
                    newCopyNumberAlteration.setReferenceGenome(referenceGenome);
                }

                sample.getCopyNumberAlterations().add(newCopyNumberAlteration);
                break;
            default:
                break;
        }
    }

    private StructuralVariantType parseDnaChangeTypeStructural(String dnaChangeType) {
        for (StructuralVariantType type : StructuralVariantType.values()) {
            if (dnaChangeType.toLowerCase().equals(dnaChangeType.toLowerCase())) {
                return type;
            }
        }
        return null;
    }

    private CopyNumberAlterationType parseDnaChangeTypeCopyNumber(String dnaChangeType) {
        for (CopyNumberAlterationType type : CopyNumberAlterationType.values()) {
            if (dnaChangeType.toLowerCase().equals(dnaChangeType.toLowerCase())) {
                return type;
            }
        }

        switch (dnaChangeType) {
            case "Copy number gain":
                return CopyNumberAlterationType.GAIN;
            case "Copy number loss":
                return CopyNumberAlterationType.LOSS;
            case "Duplication": {
                return CopyNumberAlterationType.AMPLIFICATION;
            }
            default:
                return null;
        }
    }

    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public ArrayList<Link> getLink() {
        return link;
    }
    public void setLink(ArrayList<Link> link) {
        this.link = link;
    }
    public ArrayList<Entry> getEntry() {
        return entry;
    }
    public void setEntry(ArrayList<Entry> entry) {
        this.entry = entry;
    }
}
