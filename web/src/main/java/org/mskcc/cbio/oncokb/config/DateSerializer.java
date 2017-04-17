package org.mskcc.cbio.oncokb.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hongxin on 4/17/17.
 */
public class DateSerializer extends JsonSerializer<Date> {
    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    public void serialize(Date date, JsonGenerator gen, SerializerProvider var3) throws IOException {
        gen.writeString(FORMATTER.format(date));
    }
}
