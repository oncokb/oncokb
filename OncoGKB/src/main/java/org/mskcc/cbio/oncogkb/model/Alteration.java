/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.model;

/**
 *
 * @author jgao
 */
public interface Alteration {

    String getAlteration();

    Integer getAlterationId();

    Gene getGene();

    String getType();

    void setAlteration(String alteration);

    void setAlterationId(Integer alterationId);

    void setGene(Gene gene);

    void setType(String type);
    
}
