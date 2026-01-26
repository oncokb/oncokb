package org.mskcc.cbio.oncokb.api.pvt;

import java.util.List;

import org.mskcc.cbio.oncokb.model.Tag;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TagsController {
    @RequestMapping(value = "/tags/{entrezGeneId}",
        produces = {"application/json"},
        method = RequestMethod.GET)
    public ResponseEntity<List<Tag>> getTagsByEntrezGeneId(@PathVariable int entrezGeneId) {
        return ResponseEntity.ok().body(ApplicationContextSingleton.getTagBo().findTagsByEntrezGeneId(entrezGeneId));
    }
}
