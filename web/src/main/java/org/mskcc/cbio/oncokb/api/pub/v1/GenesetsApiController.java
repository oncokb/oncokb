package org.mskcc.cbio.oncokb.api.pub.v1;

import org.mskcc.cbio.oncokb.model.Geneset;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Created by Hongxin Zhang on 2019-08-09.
 */
@Controller
public class GenesetsApiController implements GenesetsApi {
    @Override
    public ResponseEntity<List<Geneset>> genesetsGet() {
        return new ResponseEntity<>(ApplicationContextSingleton.getGenesetBo().findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Geneset> genesetsIdGet(Integer id) {
        return new ResponseEntity<>(ApplicationContextSingleton.getGenesetBo().findGenesetById(id), HttpStatus.OK);
    }
}
