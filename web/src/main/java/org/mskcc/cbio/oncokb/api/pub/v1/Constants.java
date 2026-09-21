package org.mskcc.cbio.oncokb.api.pub.v1;

/**
 * Created by Hongxin Zhang on 4/14/21.
 */
public class Constants {
    public static final String VERSION = "The data version";
    public static final String GERMLINE_VERSION = "The data version. Must be 7.1 or higher.";
    public static final String INCLUDE_EVIDENCE = "Include gene summary and background";
    public static final String HUGO_SYMBOL = "The gene symbol used in Human Genome Organisation. Gene aliases are accepted. The symbol is case-sensitive. When specified, only the curated gene matching this symbol is returned. Example: BRAF";
}
