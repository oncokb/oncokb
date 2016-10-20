/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.validation;

import java.io.InputStream;

/**
 *
 * @author jiaojiao
 */
public class ValidationConfig {
    public InputStream getStram(String propFileName){
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
            return inputStream;
        }
}
