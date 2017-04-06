/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Article;

import java.util.List;
import java.util.Map;

/**
 * @author jgao
 */
public final class NcbiEUtils {
    private NcbiEUtils() {
        throw new AssertionError();
    }

    private static final String URL_NCBI_EUTILS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

    public static Article readPubmedArticle(String pmid) {
        if (pmid != null) {
            pmid = pmid.trim();
        }
        if (!StringUtils.isNumeric(pmid)) {
            System.out.println("pmid has to be a numeric string, but the input is '" + pmid + "'");
            return null;
        }
        String url = URL_NCBI_EUTILS + "esummary.fcgi?db=pubmed&retmode=json&id=" + pmid;

        Article article = new Article(pmid);

        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            Map result = (Map) (map.get("result"));
            Map articleInfo = (Map) (result.get(pmid));

            if (articleInfo != null) {
                String pubdate = (String) (articleInfo.get("pubdate"));
                article.setPubDate(pubdate);

                if (articleInfo.get("authors") != null && !articleInfo.get("authors").getClass().equals(String.class)) {
                    List<Map<String, String>> authors = (List) (articleInfo.get("authors"));
                    article.setAuthors(formatAuthors(authors));
                } else {
                    article.setAuthors(null);
                }

                String title = (String) (articleInfo.get("title"));

                if (title == null) {
                    System.out.println("Warning: Article doesn't have a title for " + pmid);
                    return null;
                }

                article.setTitle(title);

                String volume = (String) (articleInfo.get("volume"));
                article.setVolume(volume);

                String issue = (String) (articleInfo.get("issue"));
                article.setIssue(issue);

                String pages = (String) (articleInfo.get("pages"));
                article.setPages(pages);

                String fulljournalname = (String) (articleInfo.get("fulljournalname"));
                article.setJournal(fulljournalname);

                String elocationId = (String) (articleInfo.get("elocationid"));
                article.setElocationId(elocationId);
            } else {
                System.out.println("Warning: No artical info for " + pmid);
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(url);
        }

        return article;
    }

    private static String formatAuthors(List<Map<String, String>> authors) {
        StringBuilder sb = new StringBuilder();
        sb.append(authors.get(0).get("name"));
        if (authors.size() > 1) {
            sb.append(" et al");
        }
        return sb.toString();
    }

}
