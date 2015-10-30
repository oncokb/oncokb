package org.mskcc.cbio.oncokb.controller;

import com.google.gdata.util.ServiceException;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by zhangh2 on 4/21/15.
 */
@Controller
public class GeneStatus {

    @RequestMapping(value="/api/geneStatus.json", method = {POST, GET})
    public @ResponseBody
    Object geneStatus(
            @RequestParam(value="geneId", required=true) String geneId,
            @RequestParam(value="status", required=false) String status){

        if(geneId != null) {
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            if(status != null){
                Gene gene = geneBo.findGeneByHugoSymbol(geneId);
                if(gene != null){
                    gene.setStatus(status);
                    geneBo.saveOrUpdate(gene);
                    return true;
                }else{
                    return false;
                }
            }else {
                return geneBo.findGeneByHugoSymbol(geneId);
            }
        }else {

            return false;
        }
    }
}
