package org.mskcc.cbio.oncokb.apiModels.annotation;

/**
 * Created by Hongxin Zhang on 2019-03-25.
 */
public enum AnnotationQueryType {
    WEB("web", "Summary in annotation will have hyperlinks."),
    REGULAR("regular", "Summary in annotation will only have plain text.");

    private String name;
    private String description;

    AnnotationQueryType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
