package org.mskcc.cbio.oncokb.apiModels.annotation;

import java.util.List;

public class AnnotateSampleQuery implements java.io.Serializable {
    private List<AnnotateStructuralVariantQuery> structuralVariants;
    private List<AnnotateCopyNumberAlterationQuery> copyNumberAlterations;
    private MutationsQuery mutations;

    public static class MutationsQuery {
        private List<AnnotateMutationByGenomicChangeQuery> genomicChange;
        private List cDnaChange;
        private List<AnnotateMutationByProteinChangeQuery> proteinChange;
        private List<AnnotateMutationByHGVSgQuery> hgvsg;

        public List<AnnotateMutationByGenomicChangeQuery> getGenomicChange() {
            return genomicChange;
        }

        public void setGenomicChange(List<AnnotateMutationByGenomicChangeQuery> genomicChange) {
            this.genomicChange = genomicChange;
        }

        public List getcDnaChange() {
            return cDnaChange;
        }

        public void setcDnaChange(List cDnaChange) {
            this.cDnaChange = cDnaChange;
        }

        public List<AnnotateMutationByProteinChangeQuery> getProteinChange() {
            return proteinChange;
        }

        public void setProteinChange(List<AnnotateMutationByProteinChangeQuery> proteinChange) {
            this.proteinChange = proteinChange;
        }

        public List<AnnotateMutationByHGVSgQuery> getHgvsg() {
            return hgvsg;
        }

        public void setHgvsg(List<AnnotateMutationByHGVSgQuery> hgvsg) {
            this.hgvsg = hgvsg;
        }
    }

    public List<AnnotateStructuralVariantQuery> getStructuralVariants() {
        return structuralVariants;
    }

    public void setStructuralVariants(List<AnnotateStructuralVariantQuery> structuralVariants) {
        this.structuralVariants = structuralVariants;
    }

    public List<AnnotateCopyNumberAlterationQuery> getCopyNumberAlterations() {
        return copyNumberAlterations;
    }

    public void setCopyNumberAlterations(List<AnnotateCopyNumberAlterationQuery> copyNumberAlterations) {
        this.copyNumberAlterations = copyNumberAlterations;
    }

    public MutationsQuery getMutations() {
        return mutations;
    }

    public void setMutations(MutationsQuery mutations) {
        this.mutations = mutations;
    }
}
