/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncogkb.model;

/**
 *
 * @author jgao
 */
public interface TumorType {

    String getColor();

    String getName();

    String getShortName();

    String getTumorTypeId();

    void setColor(String color);

    void setName(String name);

    void setShortName(String shortName);

    void setTumorTypeId(String tumorTypeId);
    
}
