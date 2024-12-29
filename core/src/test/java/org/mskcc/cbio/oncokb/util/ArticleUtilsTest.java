package org.mskcc.cbio.oncokb.util;

import static org.junit.Assert.assertEquals;
import static org.mskcc.cbio.oncokb.util.ArticleUtils.getAbstractFromText;
import static org.mskcc.cbio.oncokb.util.ArticleUtils.getPmidsFromText;

import java.util.*;
import org.junit.Test;
import org.mskcc.cbio.oncokb.model.Article;

public class ArticleUtilsTest {
    @Test
    public void testGetAbstractFromText() {
        assertEquals(0, getAbstractFromText("").size());
        assertEquals(0, getAbstractFromText(" ").size());
        assertEquals(0, getAbstractFromText(null).size());

        Set<Article> articleSet = getAbstractFromText("Abstract: Demetri et al. Abstract# LBA17, ESMO 2018 Congress. https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583");
        assertEquals(1, articleSet.size());
        assertEquals("Demetri et al. Abstract# LBA17, ESMO 2018 Congress.", articleSet.iterator().next().getAbstractContent());
        assertEquals("https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583", articleSet.iterator().next().getLink());

        articleSet = getAbstractFromText("(Abstract: Demetri et al. Abstract# LBA17, ESMO 2018 Congress. https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583)");
        assertEquals(1, articleSet.size());
        assertEquals("Demetri et al. Abstract# LBA17, ESMO 2018 Congress.", articleSet.iterator().next().getAbstractContent());
        assertEquals("https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583", articleSet.iterator().next().getLink());

        articleSet = getAbstractFromText("(Abstract: Demetri et al. Abstract# LBA17, ESMO 2018 Congress. https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583 PMID:123)");
        assertEquals(1, articleSet.size());
        assertEquals("Demetri et al. Abstract# LBA17, ESMO 2018 Congress.", articleSet.iterator().next().getAbstractContent());
        assertEquals("https://academic.oup.com/annonc/article/29/suppl_8/mdy424.017/5141583", articleSet.iterator().next().getLink());

        // text when multiple abstracts within the parenthesis
        articleSet = getAbstractFromText("(Abstract: AbstractA. https://linkA  Abstract: AbstractB. https://linkB)");
        assertEquals(2, articleSet.size());
        List<Article> articleList = new ArrayList<>(articleSet);
        assertEquals("AbstractA.", articleList.get(0).getAbstractContent());
        assertEquals("https://linkA", articleList.get(0).getLink());
        assertEquals("AbstractB.", articleList.get(1).getAbstractContent());
        assertEquals("https://linkB", articleList.get(1).getLink());

        // text when multiple abstracts within the parenthesis mixed with PMID
        articleSet = getAbstractFromText("(Abstract: AbstractA. https://linkA ; PMID: 123; Abstract: AbstractB. https://linkB)");
        assertEquals(2, articleSet.size());
        assertEquals("AbstractA.", articleList.get(0).getAbstractContent());
        assertEquals("https://linkA", articleList.get(0).getLink());
        assertEquals("AbstractB.", articleList.get(1).getAbstractContent());
        assertEquals("https://linkB", articleList.get(1).getLink());

        // text when abstract link has parenthesis
        articleSet = getAbstractFromText("(Abstract: Bouffet et al. Abstract# LBA2002, ASCO 2022. https://www.annalsofoncology.org/article/S0923-7534(23)03242-8/fulltext(23))");
        assertEquals(1, articleSet.size());
        assertEquals("Bouffet et al. Abstract# LBA2002, ASCO 2022.", articleSet.iterator().next().getAbstractContent());
        assertEquals("https://www.annalsofoncology.org/article/S0923-7534(23)03242-8/fulltext(23)", articleSet.iterator().next().getLink());
    }

    @Test
    public void testGetPmidsFromText() {
        assertEquals(0, getPmidsFromText("").size());
        assertEquals(0, getPmidsFromText(" ").size());
        assertEquals(0, getPmidsFromText(null).size());

        assertEquals(1, getPmidsFromText("PMID: 123").size());
        assertEquals(1, getPmidsFromText("PMIDs: 123").size());
        assertEquals(1, getPmidsFromText("pmid: 123").size());
        assertEquals(1, getPmidsFromText("pmids: 123").size());
        assertEquals(1, getPmidsFromText("PMID:123").size());
        assertEquals(1, getPmidsFromText("PMID:123 ").size());
        assertEquals(1, getPmidsFromText("PMID: 123 123").size());
        assertEquals(1, getPmidsFromText("PMID: 123 456").size());
        assertEquals(1, getPmidsFromText("PMID: 123,123").size());
        assertEquals(2, getPmidsFromText("PMID: 123,456").size());
        assertEquals(1, getPmidsFromText("(Abstract: AbstractA. https://linkA ; PMID: 123; Abstract: AbstractB. https://linkB)").size());

        assertEquals("123", getPmidsFromText("PMID: 123").iterator().next());
    }
}
