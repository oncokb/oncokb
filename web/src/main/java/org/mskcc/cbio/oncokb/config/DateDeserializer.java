package org.mskcc.cbio.oncokb.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hongxin on 4/17/17.
 */
public class DateDeserializer extends JsonDeserializer<Date> {
    public static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        try {
            return FORMATTER.parse(p.getValueAsString());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
