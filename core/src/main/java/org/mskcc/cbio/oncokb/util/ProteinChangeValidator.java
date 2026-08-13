package org.mskcc.cbio.oncokb.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.AlterationValidationError;
import org.mskcc.cbio.oncokb.model.AlterationValidationErrorType;
import org.mskcc.cbio.oncokb.util.parser.ProteinChangeParser;

/**
 * Checks a queried protein change against a gene's canonical protein sequence and, when they
 * disagree, returns a single human-readable message describing the highest-priority disagreement.
 *
 * <p>Three-letter amino-acid codes (e.g. {@code Val600Glu}) are normalized to one-letter form via
 * {@link AminoAcidConverterUtils}, and whether a string is a protein change is delegated to
 * {@link ProteinChangeParser}. Callers pass in the canonical sequence, so the class is pure.
 */
public final class ProteinChangeValidator {

    private ProteinChangeValidator() {}

    // Captures the reference residues the parser does not expose. Boundary residues are captured as
    // [A-Z]+ (not a single letter) so a malformed boundary like VVV600_W604del is recognized.
    // Groups: 1 leading ref residue(s), 2 start position, 3/4 second ref residue(s)/end position
    // (range only), 5 del/delins/ins/dup operator, 6 tail (variant residues, or spelled-out del).
    private static final Pattern REFERENCE_RESIDUES =
        Pattern.compile("^([A-Z]+)([0-9]+)(?:_([A-Z]+)([0-9]+))?(delins|del|ins|dup)?([A-Z0-9*]*)$");

    // Frameshift queries (V600fs, R123Gfs*45) carry a reference residue too, but the pattern above
    // cannot see it because of the lowercase "fs" operator, so they get their own pattern.
    // Groups: 1 ref residue, 2 position.
    private static final Pattern FRAMESHIFT_REFERENCE_RESIDUES =
        Pattern.compile("^([A-Z])([0-9]+)[A-Z]?fs(\\*([0-9]+|\\?)?)?$");

    // A spelled-out deleted sequence: amino-acid residues immediately following the lowercase "del"
    // operator, terminated by an "ins" operator or the end of the string. HGVS describes deletions by
    // position only, so these residues are redundant and are stripped by normalize.
    private static final Pattern DELETED_SEQUENCE = Pattern.compile("del([A-Z]+)(ins|$)");

    // Problems in priority order; when more than one applies, the first one wins. Each carries the
    // AlterationValidationErrorType it is reported as; several protein-change problems share one, since the
    // reported vocabulary is coarser than the checks that produce it.
    private enum Problem {
        INVALID_RANGE_BOUNDARY(AlterationValidationErrorType.MALFORMED_ALTERATION),
        INVALID_POINT_REFERENCE(AlterationValidationErrorType.MALFORMED_ALTERATION),
        POSITION_OUT_OF_RANGE(AlterationValidationErrorType.POSITION_OUT_OF_RANGE),
        REFERENCE_RESIDUE_MISMATCH(AlterationValidationErrorType.REFERENCE_ALLELE_MISMATCH),
        START_AFTER_END(AlterationValidationErrorType.REVERSED_POSITION_RANGE);

        private final AlterationValidationErrorType type;

        Problem(AlterationValidationErrorType type) {
            this.type = type;
        }
    }

    /**
     * Applies every protein-change normalization rule in turn and reports both the rewritten change and
     * which rules fired, so callers know not just <em>that</em> a query was normalized but <em>how</em>.
     * A query no rule touches comes back unchanged with an empty {@link NormalizationResult#getApplied()}.
     * The input is expected to already be in HGVSp-short form (uppercase residues, lowercase operators).
     */
    public static NormalizationResult normalize(String proteinChange) {
        List<ProteinChangeNormalization> applied = new ArrayList<>();
        String result = proteinChange;
        if (!StringUtils.isEmpty(result)) {
            // Rule: drop a spelled-out deleted sequence (A237_G238delAG -> A237_G238del). Future rules
            // append here, each operating on the running `result` and recording what they changed.
            String stripped = DELETED_SEQUENCE.matcher(result).replaceAll("del$2");
            if (!stripped.equals(result)) {
                result = stripped;
                applied.add(ProteinChangeNormalization.DELETED_SEQUENCE_DROPPED);
            }
        }
        return new NormalizationResult(result, applied);
    }

    /** The outcome of {@link #normalize(String)}: the rewritten change plus the rules that produced it. */
    public static final class NormalizationResult {
        private final String proteinChange;
        private final List<ProteinChangeNormalization> applied;

        NormalizationResult(String proteinChange, List<ProteinChangeNormalization> applied) {
            this.proteinChange = proteinChange;
            this.applied = Collections.unmodifiableList(applied);
        }

        /** The normalized protein change (equal to the input when nothing was applied). */
        public String getProteinChange() {
            return proteinChange;
        }

        /** The normalizations that fired, in the order applied; empty when the input was left as-is. */
        public List<ProteinChangeNormalization> getApplied() {
            return applied;
        }

        public boolean isNormalized() {
            return !applied.isEmpty();
        }
    }

    public static Optional<AlterationValidationError> validate(String hugoSymbol, String proteinChange, String canonicalSequence) {
        if (StringUtils.isEmpty(proteinChange) || StringUtils.isEmpty(canonicalSequence)) {
            return Optional.empty();
        }

        String normalized = AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(proteinChange);

        ReferenceContext ctx;
        Matcher matcher = REFERENCE_RESIDUES.matcher(normalized);
        if (matcher.matches()) {
            ctx = ReferenceContext.from(hugoSymbol, normalized, canonicalSequence, matcher);
        } else {
            Matcher frameshift = FRAMESHIFT_REFERENCE_RESIDUES.matcher(normalized);
            if (!frameshift.matches()) {
                return Optional.empty();
            }
            ctx = ReferenceContext.fromFrameshift(hugoSymbol, normalized, canonicalSequence, frameshift);
        }
        if (ctx == null) {
            return Optional.empty();
        }

        // A range boundary carrying more than one residue is malformed even though the parser cannot
        // parse it, so run this structural check before the parser gate below.
        Optional<String> structural = ctx.check(Problem.INVALID_RANGE_BOUNDARY);
        if (structural.isPresent()) {
            return Optional.of(new AlterationValidationError(Problem.INVALID_RANGE_BOUNDARY.type, structural.get()));
        }

        // The remaining checks interpret residues/positions, so only run them once the parser confirms
        // this is a protein change (keeps Amplification, Fusion, *_splice, etc. out).
        if (!Boolean.TRUE.equals(ProteinChangeParser.parseAlteration(normalized).getIsParsed())) {
            return Optional.empty();
        }

        for (Problem problem : Problem.values()) {
            if (problem == Problem.INVALID_RANGE_BOUNDARY) {
                continue;
            }
            Optional<String> message = ctx.check(problem);
            if (message.isPresent()) {
                return Optional.of(new AlterationValidationError(problem.type, message.get()));
            }
        }
        return Optional.empty();
    }

    /** Immutable holder for the parsed pieces of one query plus the message-building for each check. */
    private static final class ReferenceContext {
        private final String hugoSymbol;
        private final String proteinChange;
        private final String sequence;
        private final String ref1;
        private final String ref2;   // null unless the query is a range
        private final int start;
        private final int end;       // == start when not a range

        private ReferenceContext(String hugoSymbol, String proteinChange, String sequence,
                                 String ref1, String ref2, int start, int end) {
            this.hugoSymbol = hugoSymbol;
            this.proteinChange = proteinChange;
            this.sequence = sequence;
            this.ref1 = ref1;
            this.ref2 = ref2;
            this.start = start;
            this.end = end;
        }

        static ReferenceContext from(String hugoSymbol, String proteinChange, String sequence, Matcher matcher) {
            String ref1 = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            String ref2 = matcher.group(3);
            int end = matcher.group(4) == null ? start : Integer.parseInt(matcher.group(4));

            // A reversed range (end < start) is reported by START_AFTER_END, not dropped here.
            if (start < 1 || end < 1) {
                return null;
            }
            return new ReferenceContext(hugoSymbol, proteinChange, sequence, ref1, ref2, start, end);
        }

        /**
         * A frameshift is a single position carrying a single reference residue, so it is a point context:
         * the range-boundary, reversed-range and multi-residue checks can never fire on it, leaving the
         * position and the reference residue as the only two that do any work.
         */
        static ReferenceContext fromFrameshift(String hugoSymbol, String proteinChange, String sequence, Matcher matcher) {
            int start = Integer.parseInt(matcher.group(2));
            if (start < 1) {
                return null;
            }
            return new ReferenceContext(hugoSymbol, proteinChange, sequence, matcher.group(1), null, start, start);
        }

        Optional<String> check(Problem problem) {
            switch (problem) {
                case INVALID_RANGE_BOUNDARY:
                    return checkRangeBoundary();
                case INVALID_POINT_REFERENCE:
                    return checkPointReference();
                case POSITION_OUT_OF_RANGE:
                    return checkPositionInRange();
                case REFERENCE_RESIDUE_MISMATCH:
                    return checkReferenceResidues();
                case START_AFTER_END:
                    return checkStartAfterEnd();
                default:
                    return Optional.empty();
            }
        }

        private boolean isRange() {
            return ref2 != null;
        }

        private boolean isMultiResiduePoint() {
            return !isRange() && ref1.length() > 1;
        }

        private String prefix() {
            return hugoSymbol + " " + proteinChange + ": ";
        }

        Optional<String> checkRangeBoundary() {
            if (!isRange()) {
                return Optional.empty();
            }
            if (ref1.length() > 1) {
                return Optional.of(prefix() + "The reference amino acid at position " + start
                    + " must be a single amino acid instead of " + ref1 + ".");
            }
            if (ref2.length() > 1) {
                return Optional.of(prefix() + "The reference amino acid at position " + end
                    + " must be a single amino acid instead of " + ref2 + ".");
            }
            return Optional.empty();
        }

        Optional<String> checkPointReference() {
            if (isMultiResiduePoint()) {
                return Optional.of(prefix() + "Not a valid protein change.");
            }
            return Optional.empty();
        }

        Optional<String> checkPositionInRange() {
            // Use the furthest position so a reversed range is still bounded before any substring runs.
            int furthest = Math.max(start, end);
            if (furthest > sequence.length()) {
                return Optional.of(prefix() + "position " + furthest + " exceeds the " + hugoSymbol
                    + " canonical protein length of " + sequence.length() + ".");
            }
            return Optional.empty();
        }

        Optional<String> checkStartAfterEnd() {
            if (start > end) {
                return Optional.of(prefix() + "Start position " + start
                    + " is greater than end position " + end + ".");
            }
            return Optional.empty();
        }

        Optional<String> checkReferenceResidues() {
            String canonicalStart = sequence.substring(start - 1, start);
            String startRef = ref1;
            boolean startWrong = !canonicalStart.equals(startRef);

            if (isRange()) {
                String canonicalEnd = sequence.substring(end - 1, end);
                boolean endWrong = !canonicalEnd.equals(ref2);
                if (startWrong && endWrong) {
                    return Optional.of(prefix() + "The reference amino acids at positions " + start + " and " + end
                        + " are " + canonicalStart + " and " + canonicalEnd + " instead of " + startRef + " and " + ref2
                        + " on the OncoKB canonical transcript.");
                }
                if (endWrong) {
                    return Optional.of(prefix() + "The reference amino acid at position " + end
                        + " is " + canonicalEnd + " instead of " + ref2 + " on the OncoKB canonical transcript.");
                }
            }
            if (startWrong) {
                return Optional.of(prefix() + "The reference amino acid at position " + start
                    + " is " + canonicalStart + " instead of " + startRef + " on the OncoKB canonical transcript.");
            }
            return Optional.empty();
        }
    }
}
