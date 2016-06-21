/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import java.util.Date;

import org.mskcc.cbio.oncokb.model.StringResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author zhangh2
 */
@Controller
public class AccessController {
//    @RequestMapping(value="/legacy-api/access", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody
    StringResponse getAlteration() {
        Date date = new Date();
        StringResponse response = new StringResponse(date.toString());

        return response;
    }
}