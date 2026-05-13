package org.mskcc.cbio.oncokb.util.parser;

import static org.mskcc.cbio.oncokb.Constants.FRAMESHIFT_VARIANT;
import static org.mskcc.cbio.oncokb.Constants.IN_FRAME_DELETION;
import static org.mskcc.cbio.oncokb.Constants.IN_FRAME_INSERTION;
import static org.mskcc.cbio.oncokb.Constants.MISSENSE_VARIANT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mskcc.cbio.oncokb.model.AlterationPositionBoundary;
import org.mskcc.cbio.oncokb.model.FrameshiftVariant;
import org.mskcc.cbio.oncokb.util.MainUtils;

public class ProteinChangeParser {

    private static final Pattern INFRAME_PATTERN = Pattern.compile(
        "([A-Z]?)([0-9]+)(_[A-Z]?([0-9]+))?(delins|ins|del)([A-Z0-9\\*]*)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SPLICE_PATTERN = Pattern.compile(
        "[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(_)?splice",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern RANGE_PATTERN = Pattern.compile(
        "([A-Z]?([0-9]+)_[A-Z]?([0-9]+))(.+)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern POINT_PATTERN = Pattern.compile(
        "(([A-Z]+)?([0-9]+))(ins|del|dup|mut)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern N_TERMINAL_EXTENSION_PATTERN = Pattern.compile(
        "(M)?1ext(-[0-9]+)?",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern C_TERMINAL_EXTENSION_PATTERN = Pattern.compile(
        "((\\*)?([0-9]+)[A-Z]?)ext(([A-Z]+)?\\*([0-9]+)?(\\?)?)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SYNONYMOUS_PATTERN = Pattern.compile(
        "([A-Z\\*])?([0-9]+)=",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern GENERAL_PATTERN = Pattern.compile(
        "^([A-Z\\*]+)?([0-9]+)([A-Z\\*\\?]*)$",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern FRAMESHIFT_PATTERN = Pattern.compile(
        "(([A-Z\\*]+)?([0-9]+)([A-Z])?)(fs)(\\*([0-9]+|\\?)?)?",
        Pattern.CASE_INSENSITIVE
    );

    public static ParseAlterationResult parseAlteration(String proteinChange) {
        ParseAlterationResult result = parseInframe(proteinChange);
        if (result.isParsed) return result;

        result = parseSplice(proteinChange);
        if (result.isParsed) return result;
        
        result = parseRange(proteinChange);
        if (result.isParsed) return result;
        
        result = parseFrameshift(proteinChange);
        if (result.isParsed) return result;

        result = parsePoint(proteinChange);
        if (result.isParsed)  return result;
        
        result = parseExtension(proteinChange);
        if (result.isParsed) return result;

        result = parseSynonymous(proteinChange);
        if (result.isParsed) return result;

        result =  parseGeneral(proteinChange);
        if (result.isParsed) return result;

        return result;
    }

    public static ParseAlterationResult parseInframe(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = INFRAME_PATTERN.matcher(proteinChange);
        if (m.matches()) {

            result.isParsed = true;
            String ref = m.group(1) == null ? "" : m.group(1).toUpperCase();
            String range = m.group(3) == null ? "" : m.group(3).toUpperCase();
            String type = m.group(5).toLowerCase();
            String tail = m.group(6) == null ? "" : m.group(6).toUpperCase();
            result.normalizedProteinChange = ref + m.group(2) + range + type + tail;


            if (m.group(1) != null && m.group(3) == null) {
                // we only want to specify reference when it's one position ins/del
                result.ref = m.group(1).toUpperCase();
            }
            result.start = Integer.valueOf(m.group(2));
            if (m.group(4) != null) {
                result.end = Integer.valueOf(m.group(4));
            } else {
                result.end = result.start;
            }
            if (type.equals("ins")) {
                result.consequence = IN_FRAME_INSERTION;
            } else if (type.equals("del")) {
                result.consequence = IN_FRAME_DELETION;
            } else {
                // this will be delins, it requires AA after delins to be specified, otherwise, you won't be able to know its consequence
                Integer deletion = result.end - result.start + 1;
                String groupSix = m.group(6);
                String groupSixWithoutDigits = MainUtils.removeDigits(groupSix);

                if (groupSixWithoutDigits.contains("*")) {
                    result.consequence = "stop_gained";
                } 
                else if (groupSixWithoutDigits.length() != groupSix.length() && groupSixWithoutDigits.length() > 0) {
                    if (groupSixWithoutDigits.length() > deletion) {
                        result.consequence = IN_FRAME_INSERTION;
                    } else {
                        result.consequence = "NA";
                    }
                } else {
                    Integer insertion = groupSix.length();
                    if (groupSixWithoutDigits.length() == 0 && insertion > 0) {
                        insertion = Integer.parseInt(groupSix);
                    }
                    if (insertion == 0) {
                        result.consequence = "NA";
                    } else if (insertion - deletion > 0) {
                        result.consequence = IN_FRAME_INSERTION;
                    } else if (insertion - deletion == 0) {
                        result.consequence = MISSENSE_VARIANT;
                    } else {
                        result.consequence = IN_FRAME_DELETION;
                    }
                }
            }
        }
        return result;
    }

    public static ParseAlterationResult parseSplice(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = SPLICE_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            
            result.isParsed = true;
            String[] parts = m.group(0).toLowerCase().split("splice", 2);
            result.normalizedProteinChange = parts[0].toUpperCase() + "splice";

            result.start = Integer.valueOf(m.group(1));
            if (m.group(3) != null) {   // Group 3 existing means that it is a range
                result.end = Integer.valueOf(m.group(3));
            } else {
                result.end = result.start;
            }
            result.consequence = "splice_region_variant";
        }
        return result;
    }

    public static ParseAlterationResult parseRange(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = RANGE_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            result.isParsed = true;
            result.start = Integer.valueOf(m.group(2));
            result.end = Integer.valueOf(m.group(3));
            String type = m.group(4).toLowerCase();

            result.normalizedProteinChange = m.group(1).toUpperCase() + m.group(4).toLowerCase();

            String consequence = "NA";
            switch (type) {
                case "mis":
                    consequence = MISSENSE_VARIANT;
                    break;
                case "ins":
                    consequence = IN_FRAME_INSERTION;
                    break;
                case "del":
                    consequence = IN_FRAME_DELETION;
                    break;
                case "fs":
                    consequence = FRAMESHIFT_VARIANT;
                    break;
                case "trunc":
                    consequence = "feature_truncation";
                    break;
                case "dup":
                    consequence = IN_FRAME_INSERTION;
                    break;
                case "mut":
                    consequence = "any";
            }
            result.consequence = consequence;
        }
        return result;
    }

    public static ParseAlterationResult parseFrameshift(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        FrameshiftVariant frameshiftVariant = parseFrameshiftVariant(proteinChange);
        if (frameshiftVariant != null) {
            result.isParsed = true;
            Matcher m = FRAMESHIFT_PATTERN.matcher(proteinChange);
            if (m.matches()) {
                String prefix = m.group(1) == null ? "" : m.group(1).toUpperCase(); // E643F
                String suffix = m.group(6) == null ? "" : m.group(6); // "*4"
                result.normalizedProteinChange = prefix + "fs" + suffix; // E643F + fs + *4
            }
            result.ref = frameshiftVariant.getRefResidues();
            result.start = frameshiftVariant.getProteinStart();
            result.end = result.start;
            result.consequence = FRAMESHIFT_VARIANT;
        }
        return result;
    }

    public static FrameshiftVariant parseFrameshiftVariant(String proteinChange) {
        if (proteinChange == null || proteinChange.isEmpty()) {
            return null;
        }
        Matcher m = FRAMESHIFT_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            FrameshiftVariant variant = new FrameshiftVariant();
            variant.setRefResidues(m.group(2) == null ? "" : m.group(2).toUpperCase());
            variant.setProteinStart(Integer.valueOf(m.group(3)));
            variant.setProteinEnd(variant.getProteinStart());
            variant.setVariantResidues(m.group(4) == null ? "" : m.group(4).toUpperCase());
            variant.setExtension(m.group(7) == null ? "" : m.group(7).toUpperCase());
            return variant;
        }
        return null;
    }

    public static ParseAlterationResult parsePoint(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = POINT_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            result.isParsed = true;
            result.ref = m.group(2) == null ? null : m.group(2).toUpperCase();
            result.start = Integer.valueOf(m.group(3));
            result.end = result.start;

            result.normalizedProteinChange = m.group(1).toUpperCase() + m.group(4).toLowerCase(); // V600 + del

            String type = m.group(4).toLowerCase();
            String consequence = "NA";
            switch (type) {
                case "ins":
                    consequence = IN_FRAME_INSERTION;
                    break;
                case "dup":
                    consequence = IN_FRAME_INSERTION;
                    break;
                case "del":
                    consequence = IN_FRAME_DELETION;
                    break;
                case "mut":
                    consequence = "any";
                    break;
            }
            result.consequence = consequence;
        }
        return result;
    }

    public static ParseAlterationResult parseExtension(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = N_TERMINAL_EXTENSION_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            result.isParsed = true;
            result.normalizedProteinChange = (m.group(1) == null ? "" : "M")
                + "1ext"
                + (m.group(2) == null ? "" : m.group(2));
            
            result.start = 1;
            result.end = result.start;
            result.consequence = IN_FRAME_INSERTION;
        } else {
            /**
             * support extension variant (https://varnomen.hgvs.org/recommendations/protein/variant/extension/)
             * the following examples are supported
             * *959Qext*14
             * *110Gext*17
             * *315TextALGT*
             * *327Aext*?
             */
            m = C_TERMINAL_EXTENSION_PATTERN.matcher(proteinChange);
            if (m.matches()) {
                result.isParsed = true;
                result.normalizedProteinChange = m.group(1).toUpperCase() + "ext" + m.group(4).toUpperCase();

                result.ref = m.group(2) == null ? "" : m.group(2).toUpperCase();
                result.start = Integer.valueOf(m.group(3));
                result.end = result.start;
                result.consequence = "stop_lost";
            }
        } 
        return result;
    }

    public static ParseAlterationResult parseSynonymous(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = SYNONYMOUS_PATTERN.matcher(proteinChange);
        if (m.matches()) {
            result.isParsed = true;
            result.normalizedProteinChange = m.group(0).toUpperCase();

            result.var = result.ref = m.group(1) == null ? "" : m.group(1).toUpperCase();
            result.start = Integer.valueOf(m.group(2));
            result.end = result.start;
            if ("*".equals(result.ref)) {
                result.consequence = "stop_retained_variant";
            } else {
                result.consequence = "synonymous_variant";
            }
        }
        return result;
    }

    public static ParseAlterationResult parseGeneral(String proteinChange) {
        ParseAlterationResult result = new ParseAlterationResult();
        Matcher m = GENERAL_PATTERN.matcher(proteinChange);
        String ref = null, var = null, consequence = "NA";
        Integer start = AlterationPositionBoundary.START.getValue(), end = AlterationPositionBoundary.END.getValue();
        if (m.matches()) {
            result.isParsed = true;
            result.normalizedProteinChange = proteinChange.toUpperCase();

            ref = m.group(1) == null ? "" : m.group(1).toUpperCase();
            start = Integer.valueOf(m.group(2));
            end = start;
            var = m.group(3).toUpperCase();

            Integer refL = ref.length();
            Integer varL = var.length();

            if (ref.equals("*")) {
                consequence = "stop_lost";
            } else if (var.equals("*")) {
                consequence = "stop_gained";
            } else if (ref.equalsIgnoreCase(var)) {
                consequence = "synonymous_variant";
            } else if (start == 1) {
                consequence = "start_lost";
            } else if (var.equals("?")) {
                consequence = "any";
            } else {
                end = start + refL - 1;
                if (refL > 1 || varL > 1) {
                    // Handle in-frame insertion/deletion event. Exp: IK744K
                    if (refL > varL) {
                        consequence = IN_FRAME_DELETION;
                    } else if (refL < varL) {
                        consequence = IN_FRAME_INSERTION;
                    } else {
                        consequence = MISSENSE_VARIANT;
                    }
                } else if (refL == 1 && varL == 1) {
                    consequence = MISSENSE_VARIANT;
                } else {
                    consequence = "NA";
                }
            }
        }
        result.consequence = consequence;
        result.ref = ref;
        result.var = var;
        result.start = start;
        result.end = end;
        return result;                       
    }

}
