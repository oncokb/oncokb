package org.mskcc.cbio.oncokb.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Article;

public class ArticleUtils {
    public static LinkedHashSet<Article> getAbstractFromText(String text) {
        LinkedHashSet<Article> articles = new LinkedHashSet<>();
        if (StringUtils.isEmpty(text)) return articles;
        Pattern abstractPattern = Pattern.compile("abstract:?([^\\)]*?)(\\bhttps?:\\/\\/[\\w-.]+(\\S*)?\\b)", Pattern.CASE_INSENSITIVE);
        Matcher abstractMatch = abstractPattern.matcher(text);
        int start = 0;
        while (abstractMatch.find(start)) {
            String abContent = abstractMatch.group(1).trim();
            String abLink = abstractMatch.group(2).trim();

            // Due to the shortfall of the regex, we need to back-fill the closing parenthesis
            int numOpenParenthesis = StringUtils.countMatches(abLink, "(");
            int numCloseParenthesis = StringUtils.countMatches(abLink, ")");
            int diff = numOpenParenthesis - numCloseParenthesis;

            if (diff > 0) abLink += String.join("", Collections.nCopies(diff, ")"));
            if (!abContent.isEmpty()) {
                Article doc = new Article();
                doc.setAbstractContent(abContent);
                doc.setLink(abLink);
                articles.add(doc);
            }
            start = abstractMatch.end();
        }

        return articles;
    }

    public static LinkedHashSet<String> getPmidsFromText(String text) {
        LinkedHashSet<String> pmids = new LinkedHashSet<>();
        if (StringUtils.isEmpty(text)) return pmids;
        Pattern pmidPattern = Pattern.compile("PMIDs?:?\\s*([\\d,\\s*]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = pmidPattern.matcher(text);
        int start = 0;
        while (m.find(start)) {
            String pmidsStr = m.group(1).trim();
            for (String pmid : pmidsStr.split(", *(PMID:)? *")) {
                pmids.add(pmid.trim());
            }
            start = m.end();
        }
        return pmids;
    }
}
