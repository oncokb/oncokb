
package org.mskcc.cbio.oncokb.bo;


import org.mskcc.cbio.oncokb.model.Info;

/**
 *
 * @author Hongxin Zhang
 */
public interface InfoBo extends GenericBo<Info> {
    Info get();

    void update(Info info);
}
