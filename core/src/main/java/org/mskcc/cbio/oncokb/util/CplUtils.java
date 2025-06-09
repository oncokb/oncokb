package org.mskcc.cbio.oncokb.util;

import static org.mskcc.cbio.oncokb.util.SummaryUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.model.ReferenceGenome;
import org.mskcc.cbio.oncokb.model.TumorType;

// This file stores all logics related to OncoKB Curation Programming Language
// See https://sop.oncokb.org/ Chapter 6, Protocol 8.
public class CplUtils {

    public static class ParseResult {
        public Map<String, List<String>> extractedModifiers;
        public String cleanedText;

        public ParseResult(Map<String, List<String>> extractedModifiers, String cleanedText) {
            this.extractedModifiers = extractedModifiers;
            this.cleanedText = cleanedText;
        }
    }

    private static final Map<String, String> VALID_KEYS = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("2:gene", "gene");
        put("2:mutation", "mutation");
        put("2:tumor_type", "tumor_type");
        put("3:mutation", "literal_mutation");
        put("3:mutant", "literal_mutant");
    }});

    private static final Pattern PATTERN = Pattern.compile("(\\[\\[+)([a-zA-Z0-9 _|]+)(\\]\\]+)");

    public static ParseResult extractModifiersAndCleanTemplate(String input) {
        Map<String, List<String>> result = new HashMap<>();
        StringBuffer cleanedTextBuffer = new StringBuffer();
        Matcher matcher = PATTERN.matcher(input);
    
        while (matcher.find()) {
            String openBrackets = matcher.group(1);
            String closeBrackets = matcher.group(3);
            String rawVarAndModifiers = matcher.group(2).trim();
            int bracketCount = openBrackets.length();
    
            // Split on |, but keep the first part as the variable name
            String[] parts = rawVarAndModifiers.split("\\|");
            String rawVar = parts[0].trim().replaceAll(" +", "_").toLowerCase();
            String key = VALID_KEYS.get(bracketCount + ":" + rawVar);
    
            if (key == null) continue;
    
            result.computeIfAbsent(key, k -> new ArrayList<>());
    
            // Add modifiers if present (from index 1 onward)
            for (int i = 1; i < parts.length; i++) {
                String mod = parts[i].trim();
                if (!mod.isEmpty()) {
                    result.get(key).add(mod);
                }
            }

            // Replace the original with brackets + raw variable only
            String cleaned = openBrackets + parts[0].trim() + closeBrackets;
            matcher.appendReplacement(cleanedTextBuffer, Matcher.quoteReplacement(cleaned));
        }

        matcher.appendTail(cleanedTextBuffer);

        return new ParseResult(result, cleanedTextBuffer.toString());

    }

    /**
     * Annotate CPL on gene level
     *
     * @param text       Text to annotate
     * @param hugoSymbol Gene hugo symbol which will be used to replace [[gene]]
     * @return
     */
    public static String annotateGene(String text, String hugoSymbol) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        return text.replace("[[gene]]", hugoSymbol);
    }

    /**
     * Annotate CPL
     *
     * @param text             Text to annotate
     * @param queryHugoSymbol  hugo symbol in query
     * @param queryAlteration  alteration in query
     * @param queryCancerType  cancer type in query
     * @param referenceGenome  reference genome this alteration belongs to. null if no preference
     * @param gene             Gene model that can be linked to queryHugoSymbol
     * @param matchedTumorType TumorType model that can be linked to queryCancerType
     * @param escapeNewLine    If true, replace newline with the literal "\n"
     * @return
     */
   
    public static String annotate(String text, String queryHugoSymbol, String queryAlteration, String queryCancerType, ReferenceGenome referenceGenome, Gene gene, TumorType matchedTumorType, Boolean escapeNewLine) {
        if (StringUtils.isEmpty(text))
            return "";

        // normalize queries
        if (queryHugoSymbol == null)
            queryHugoSymbol = "";
        if (queryAlteration == null)
            queryAlteration = "";
        if (queryCancerType == null)
            queryCancerType = "";

        ParseResult parseResult = extractModifiersAndCleanTemplate(text);
        Map<String, List<String>> modifiersMap = parseResult.extractedModifiers;
        text = parseResult.cleanedText;

        String interpolatedQueryAlteration = queryAlteration;
        if (modifiersMap.containsKey("mutation")) {
            interpolatedQueryAlteration = CplUtils.applyModifiers(queryAlteration, modifiersMap.get("mutation"));
        }
        String altName = getGeneMutationNameInTumorTypeSummary(gene, referenceGenome, queryHugoSymbol, queryAlteration, modifiersMap);
        String alterationName = getGeneMutationNameInVariantSummary(gene, referenceGenome, queryHugoSymbol, queryAlteration, modifiersMap);
        String tumorTypeName = convertTumorTypeNameInSummary(matchedTumorType == null ? queryCancerType : (StringUtils.isEmpty(matchedTumorType.getSubtype()) ? matchedTumorType.getMainType() : matchedTumorType.getSubtype()));

        if (tumorTypeName == null)
            tumorTypeName = "";

        String variantStr = altName;
        if (StringUtils.isNotEmpty(tumorTypeName)) {
            if (queryAlteration.contains("deletion")) {
                variantStr = tumorTypeName + " harboring a " + altName;
            } else {
                variantStr += " " + tumorTypeName;
            }
        }
        text = text.replace("[[variant]]", variantStr);
        text = text.replace("[[gene]] [[mutation]] [[[mutation]]]", alterationName);
        text = text.replace("[[gene]] [[mutation]] [[[mutant]]]", altName);

        // If the mutation already includes the gene name, we should skip the gene
        if (text.contains("[[gene]] [[mutation]]") && queryAlteration.toLowerCase().contains(queryHugoSymbol.toLowerCase())) {
            text = text.replace("[[gene]]", "");
        }

        text = text.replace("[[gene]]", queryHugoSymbol);

        // Improve false tolerance. Curators often use hugoSymbol directly instead of [[gene]]
        String specialLocationAlt = queryHugoSymbol + " [[mutation]] [[[mutation]]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = queryHugoSymbol + " [[mutation]] [[mutation]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, alterationName);
        }
        specialLocationAlt = queryHugoSymbol + " [[mutation]] [[mutant]]";
        if (text.contains(specialLocationAlt)) {
            text = text.replace(specialLocationAlt, altName);
        }

        text = text.replace("[[mutation]] [[[mutation]]]", alterationName);
        text = text.replace("[[mutation]]", interpolatedQueryAlteration);
        if (StringUtils.isNotEmpty(tumorTypeName)) {
            text = text.replace("[[tumor type]]", tumorTypeName);
        }
        text = text.replace("[[fusion name]]", altName);

        // Replace all whitespace except newlines
        String trimmedText = text.trim().replaceAll("[^\\S\\n]+", " ");
        if (Boolean.TRUE.equals(escapeNewLine)) {
            trimmedText = trimmedText.replaceAll("\\r?\\n", "\\\\n");
        }
        return trimmedText;
    }

    public static String applyModifiers(String value, List<String> modifiers) {
        Inflector inflector = Inflector.getInstance();
        for (String modifier : modifiers) {
            switch (modifier.toLowerCase()) {
                case "plural":
                    value = inflector.pluralize(value);
                    break;
                case "singular":
                    value = inflector.singularize(value);
                    break;
                default:
                    // Ignore unknown modifiers
                    break;
            }
        }
        return value;
    }
}
