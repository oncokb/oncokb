/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.util;

import java.util.List;
import java.util.Map;
import org.mskcc.cbio.oncokb.model.Article;

/**
 *
 * @author jgao
 */
public final class NcbiEUtils {
    private NcbiEUtils() {
        throw new AssertionError();
    }
    
    private static final String URL_NCBI_EUTILS = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    
    public static Article readPubmedArticle(String pmid) {
        String url = URL_NCBI_EUTILS + "esummary.fcgi?db=pubmed&retmode=json&id="+pmid;
        
        Article article = new Article(pmid);
        
        try {
            String json = FileUtils.readRemote(url);
            Map map = JsonUtils.jsonToMap(json);
            Map result = Map.class.cast(map.get("result"));
            Map articleInfo = Map.class.cast(result.get(pmid));
            
            String pubdate = String.class.cast(articleInfo.get("pubdate"));
            article.setPubDate(pubdate);
            
            List<Map<String, String>> authors = List.class.cast(articleInfo.get("authors"));
            article.setAuthors(formatAuthors(authors));
            
            String title = String.class.cast(articleInfo.get("title"));
            article.setTitle(title);
            
            String volume = String.class.cast(articleInfo.get("volume"));
            article.setVolume(volume);
            
            String issue = String.class.cast(articleInfo.get("issue"));
            article.setIssue(issue);
            
            String pages = String.class.cast(articleInfo.get("pages"));
            article.setPages(pages);
            
            String fulljournalname = String.class.cast(articleInfo.get("fulljournalname"));
            article.setJournal(fulljournalname);
            
            String elocationId = String.class.cast(articleInfo.get("elocationid"));
            article.setElocationId(elocationId);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return article;
    }
    
    private static String formatAuthors(List<Map<String, String>> authors) {
        StringBuilder sb = new StringBuilder();
        sb.append(authors.get(0).get("name"));
        if (authors.size()>1) {
            sb.append(" et al");
        }
        return sb.toString();
    }
    
}
