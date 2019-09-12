package org.mskcc.cbio.oncokb.api.pub.v1;

import io.swagger.annotations.ApiParam;
import org.mskcc.cbio.oncokb.model.Geneset;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

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
    public ResponseEntity<Geneset> genesetsUuidGet(
        @ApiParam(value = "Geneset UUID", required = true) @PathVariable(value = "uuid") String uuid
    ) {
        return new ResponseEntity<>(ApplicationContextSingleton.getGenesetBo().findGenesetByUuid(uuid), HttpStatus.OK);
    }
}
