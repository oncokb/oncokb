/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.model.Article;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jgao
 */
public final class NcbiEUtils {
    private NcbiEUtils() {
        throw new AssertionError();
    }

    private static final String URL_NCBI_EUTILS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

    private static Set<String> purifyInput(Set<String> pmids) {
        Set<String> purified = new HashSet<>();
        for (String pmid : pmids) {
            if (pmid == null)
                continue;

            pmid = pmid.trim();

            if (pmid.isEmpty())
                continue;

            if (!StringUtils.isNumeric(pmid)) {
                System.out.println("pmid has to be a numeric string, but the input is '" + pmid + "'");
            } else {
                purified.add(pmid);
            }
        }
        return purified;
    }

    public static Set<Article> readPubmedArticles(Set<String> pmids) {
        String apiKey = PropertiesUtils.getProperties("ncbi.api.key");

        try {
            if (apiKey == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("NCBI API KEY needs to be specified. Please see here for details: https://www.ncbi.nlm.nih.gov/books/NBK25497/");
            e.printStackTrace();
        }
        Set<Article> results = new HashSet<>();

        if (pmids == null) {
            return results;
        }

        pmids = purifyInput(pmids);

        if (pmids.isEmpty()) {
            return results;
        }

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        String url = URL_NCBI_EUTILS + "esummary.fcgi?api_key=" + apiKey + "&db=pubmed&retmode=json&id=" + MainUtils.listToString(new ArrayList<>(pmids), ",");
        System.out.println("Making a NCBI request at " + dateFormat.format(date) + " " + url);

        Map result = null;
        try {
            String json = FileUtils.readRemote(url);

            Map map = JsonUtils.jsonToMap(json);
            result = (Map) (map.get("result"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == null) {
            return results;
        }
        for (String pmid : pmids) {
            Article article = new Article(pmid);
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
                    continue;
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

                results.add(article);
            } else {
                System.out.println("Warning: No article info for " + pmid);
            }
        }

        return results;
    }

    private static String formatAuthors(List<Map<String, String>> authors) {
        StringBuilder sb = new StringBuilder();
        if (authors != null && authors.size() > 0) {
            sb.append(authors.get(0).get("name"));
            if (authors.size() > 1) {
                sb.append(" et al");
            }
        }
        return sb.toString();
    }

}
