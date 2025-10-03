package org.mskcc.cbio.oncokb.serializer;

import org.mskcc.cbio.oncokb.Constants;
import com.fasterxml.jackson.databind.util.StdConverter;

public class EntrezGeneIdConverter extends StdConverter<Integer, Integer> {

    @Override
    public Integer convert(Integer value) {
        if (value == null || value.equals(Constants.OTHER_BIOMARKERS_ENTREZ_GENE_ID)) {
            return value;
        }
        return Math.abs(value);
    }
    
}
