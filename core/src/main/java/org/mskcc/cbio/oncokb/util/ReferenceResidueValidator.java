package org.mskcc.cbio.oncokb.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.util.parser.ProteinChangeParser;

/**
 * Checks a queried protein change against a gene's canonical protein sequence and, when they
 * disagree, returns a single human-readable message describing the highest-priority disagreement.
 *
 * <p>Three-letter amino-acid codes (e.g. {@code Val600Glu}) are normalized to one-letter form via
 * {@link AminoAcidConverterUtils}, and whether a string is a protein change is delegated to
 * {@link ProteinChangeParser}. Callers pass in the canonical sequence, so the class is pure.
 */
public final class ReferenceResidueValidator {

    private ReferenceResidueValidator() {}

    // Captures the reference residues the parser does not expose. Boundary residues are captured as
    // [A-Z]+ (not a single letter) so a malformed boundary like VVV600_W604del is recognized.
    // Groups: 1 leading ref residue(s), 2 start position, 3/4 second ref residue(s)/end position
    // (range only), 5 del/delins/ins/dup operator, 6 tail (variant residues, or spelled-out del).
    private static final Pattern REFERENCE_RESIDUES =
        Pattern.compile("^([A-Z]+)([0-9]+)(?:_([A-Z]+)([0-9]+))?(delins|del|ins|dup)?([A-Z0-9*]*)$");

    // Problems in priority order; when more than one applies, the first one wins.
    private enum Problem {
        INVALID_RANGE_BOUNDARY,
        INVALID_POINT_REFERENCE,
        POSITION_OUT_OF_RANGE,
        REFERENCE_RESIDUE_MISMATCH,
        START_AFTER_END,
        DELETED_SEQUENCE_MISMATCH
    }

    /**
     * @param hugoSymbol        gene symbol, used only to make the message readable
     * @param proteinChange     the queried protein change (e.g. {@code A600E}, {@code A237_G238del}).
     *                          Three-letter amino-acid codes (e.g. {@code Val600Glu}) are accepted.
     * @param canonicalSequence the gene's canonical protein sequence, or {@code null}/empty if unknown
     * @return a message describing the highest-priority disagreement, or {@link Optional#empty()} when
     *         everything agrees or the check cannot be performed
     */
    public static Optional<String> validate(String hugoSymbol, String proteinChange, String canonicalSequence) {
        if (StringUtils.isEmpty(proteinChange) || StringUtils.isEmpty(canonicalSequence)) {
            return Optional.empty();
        }

        String normalized = AminoAcidConverterUtils.resolveHgvspShortFromHgvsp(proteinChange);

        Matcher matcher = REFERENCE_RESIDUES.matcher(normalized);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        ReferenceContext ctx = ReferenceContext.from(hugoSymbol, normalized, canonicalSequence, matcher);
        if (ctx == null) {
            return Optional.empty();
        }

        // A range boundary carrying more than one residue is malformed even though the parser cannot
        // parse it, so run this structural check before the parser gate below.
        Optional<String> structural = ctx.check(Problem.INVALID_RANGE_BOUNDARY);
        if (structural.isPresent()) {
            return structural;
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
                return message;
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
        private final String operator;
        private final String tail;
        private final int start;
        private final int end;       // == start when not a range

        private ReferenceContext(String hugoSymbol, String proteinChange, String sequence,
                                 String ref1, String ref2, String operator, String tail,
                                 int start, int end) {
            this.hugoSymbol = hugoSymbol;
            this.proteinChange = proteinChange;
            this.sequence = sequence;
            this.ref1 = ref1;
            this.ref2 = ref2;
            this.operator = operator;
            this.tail = tail;
            this.start = start;
            this.end = end;
        }

        static ReferenceContext from(String hugoSymbol, String proteinChange, String sequence, Matcher matcher) {
            String ref1 = matcher.group(1);
            int start = Integer.parseInt(matcher.group(2));
            String ref2 = matcher.group(3);
            int end = matcher.group(4) == null ? start : Integer.parseInt(matcher.group(4));
            String operator = matcher.group(5) == null ? "" : matcher.group(5);
            String tail = matcher.group(6) == null ? "" : matcher.group(6);

            // A reversed range (end < start) is reported by START_AFTER_END, not dropped here.
            if (start < 1 || end < 1) {
                return null;
            }
            return new ReferenceContext(hugoSymbol, proteinChange, sequence, ref1, ref2, operator, tail, start, end);
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
                case DELETED_SEQUENCE_MISMATCH:
                    return checkDeletedSequence();
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

        private String canonicalSpan() {
            return sequence.substring(start - 1, end);
        }

        Optional<String> checkRangeBoundary() {
            if (!isRange()) {
                return Optional.empty();
            }
            if (ref1.length() > 1) {
                return Optional.of(prefix() + "the reference amino acid at position " + start
                    + " must be a single amino acid, but is " + ref1 + ".");
            }
            if (ref2.length() > 1) {
                return Optional.of(prefix() + "the reference amino acid at position " + end
                    + " must be a single amino acid, but is " + ref2 + ".");
            }
            return Optional.empty();
        }

        Optional<String> checkPointReference() {
            if (isMultiResiduePoint()) {
                return Optional.of(prefix() + "not a valid protein change.");
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
                return Optional.of(prefix() + "start position " + start
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
                    return Optional.of(prefix() + "reference amino acids at positions " + start + " and " + end
                        + " are " + canonicalStart + " and " + canonicalEnd + ", not " + startRef + " and " + ref2 + ".");
                }
                if (endWrong) {
                    return Optional.of(prefix() + "reference amino acid at position " + end
                        + " is " + canonicalEnd + ", not " + ref2 + ".");
                }
            }
            if (startWrong) {
                return Optional.of(prefix() + "reference amino acid at position " + start
                    + " is " + canonicalStart + ", not " + startRef + ".");
            }
            return Optional.empty();
        }

        Optional<String> checkDeletedSequence() {
            // Only a spelled-out del<SEQ> names reference residues; delins/ins/dup tails are inserted.
            if (!"del".equals(operator) || tail.isEmpty()) {
                return Optional.empty();
            }
            int span = end - start + 1;
            if (tail.length() != span) {
                return Optional.of(prefix() + "positions " + start + "-" + end + " span " + span
                    + " residues, but the specified deleted sequence " + tail + " has " + tail.length()
                    + " residue(s).");
            }
            if (!tail.equals(canonicalSpan())) {
                return Optional.of(prefix() + "the deleted sequence " + tail
                    + " does not match the canonical residues " + canonicalSpan()
                    + " at positions " + start + "-" + end + ".");
            }
            return Optional.empty();
        }
    }
}
