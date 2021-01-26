package org.mskcc.cbio.oncokb.importer;

import org.mskcc.cbio.oncokb.model.Info;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Hongxin Zhang
 */
public final class InfoImporter {
    private static final String ONCO_TREE_VERSION = "oncotree_2019_12_01";
    private static final String NCIT_VERSION = "19.03d";

    private static final String DATA_VERSION = "v3.0";
    private static final String DATA_VERSION_DATE = "01/14/2021";


    public static void main(String[] args) throws Exception {
        Info info = new Info();
        info.setDataVersion(DATA_VERSION);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        String dateInString = DATA_VERSION_DATE;
        Date date = formatter.parse(dateInString);
        info.setDataVersionDate(date);

        info.setNcitVersion(NCIT_VERSION);
        info.setOncoTreeVersion(ONCO_TREE_VERSION);

        ApplicationContextSingleton.getInfoBo().save(info);
    }
}
