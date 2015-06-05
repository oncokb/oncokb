/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author zhangh2
 */
@Controller
public class AccessController {
    @RequestMapping(value="/access")
    public @ResponseBody String getAlteration() {
        
        Date date = new Date();
        return date.toString();
    }
}
