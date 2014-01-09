/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.model;

import java.util.Set;

/**
 *
 * @author jgao
 */
public interface Gene {

    int getEntrezGeneId();

    Set<String> getGeneAliases();

    Set<String> getGeneLabels();

    String getHugoSymbol();

    String getName();

    String getSummary();

    void setEntrezGeneId(int entrezGeneId);

    void setGeneAliases(Set<String> geneAliases);

    void setGeneLabels(Set<String> geneLabels);

    void setHugoSymbol(String hugoSymbol);

    void setName(String name);

    void setSummary(String summary);
    
}
