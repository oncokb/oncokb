/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.controller;

import org.mskcc.cbio.oncokb.util.CacheUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangh2
 */
@Controller
public class CacheController {
    @RequestMapping(value = "/legacy-api/cache", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    Map<String, String> getAlteration(
            HttpMethod method,
            @RequestParam(value = "cmd", required = false) String cmd
    ) {
        Map<String, String> result = new HashMap<>();
        switch (cmd) {
            case "getStatus":
                result.put("status", getStatus());
                break;
            default:
                break;
        }

        return result;
    }

    @RequestMapping(value = "/legacy-api/cache", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    Map<String, String> postAlteration(
            HttpMethod method,
            @RequestParam(value = "cmd", required = false) String cmd
    ) {
        Map<String, String> result = new HashMap<>();
        switch (cmd) {
            case "reset":
                resetCache();
                break;
            case "enable":
                disableCache(false);
                break;
            case "disable":
                disableCache(true);
                break;
            default:
                break;
        }
        result.put("status", "success");
        return result;
    }

    private String getStatus() {
        return CacheUtils.getCacheUtilsStatus();
    }

    private Boolean resetCache() {
        Boolean operation = true;
        try {
            CacheUtils.resetAll();
        } catch (Exception e) {
            operation = false;
        }
        return operation;
    }

    private Boolean disableCache(Boolean cmd) {
        Boolean operation = true;
        try {
            if(cmd) {
                CacheUtils.disableCacheUtils();
            }else {
                CacheUtils.enableCacheUtils();
            }
        }catch (Exception e) {
            operation = false;
        }
        return operation;
    }
}
