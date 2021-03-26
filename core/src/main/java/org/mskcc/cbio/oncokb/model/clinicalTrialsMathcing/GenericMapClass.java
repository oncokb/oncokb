package org.mskcc.cbio.oncokb.model.clinicalTrialsMathcing;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gdata.util.ParseException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.mskcc.cbio.oncokb.util.ClinicalTrialsUtils;
import org.mskcc.cbio.oncokb.util.GzipUtils;
import org.mskcc.cbio.oncokb.util.S3Utils;

public class GenericMapClass<T> {

    private static final String S3_DIR = "drug-matching/";
    private static final String S3_BUCKET = "oncokb";
    private static final String LOCAL_DIR = "/data/clinicalTrials/";

    private Map<String, T> map;
    private TypeToken<Map<String, T>> typeToken;

    public GenericMapClass(TypeToken<Map<String, T>> typeToken) {
        this.typeToken = typeToken;
    }

    public Map<String, T> getMap(String fileName)
        throws UnsupportedEncodingException, IOException, ParseException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (S3Utils.getInstance().isPropertiesConfigured()) {
            S3ObjectInputStream inputStream = S3Utils
                .getInstance()
                .getObject(S3_BUCKET, S3_DIR + fileName)
                .get()
                .getObjectContent();
            GzipUtils.deCompress(inputStream, os);
        } else if (ClinicalTrialsUtils.getInstance().isLocalFilesExisted()) {
            GzipUtils.deCompress(
                ClinicalTrialsUtils.class.getResourceAsStream(
                        LOCAL_DIR + fileName
                    ),
                os
            );
        }
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
        map = gson.fromJson(os.toString("UTF-8"), typeToken.getType());
        os.close();
        return map;
    }
}
