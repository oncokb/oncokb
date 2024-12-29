package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.Set;

/**
 * Created by Hongxin Zhang on 2/12/18.
 */

@Controller
public class ClassificationApiController implements ClassificationApi {
    @Override
    public ResponseEntity<Set<String>> classificationVariantsGet() {
        return new ResponseEntity<>(AlterationUtils.getGeneralVariants(), HttpStatus.OK);
    }
}
