package org.mskcc.cbio.oncokb.util;

import org.mskcc.cbio.oncokb.model.Evidence;
import org.mskcc.cbio.oncokb.model.Alteration;
import java.util.*;

/**
 * Created by Hongxin on 8/10/15.
 */
public class EvidenceUtils {

    /**
     * Remove evidences if its alteration in the alteration list
     * @param evidences
     * @param alterations
     * @return
     */
    public static List<Evidence> removeByAlterations(List<Evidence> evidences, Collection<Alteration> alterations) {
        if(alterations != null) {
            Iterator<Evidence> i = evidences.iterator();
            while (i.hasNext()) {
                Boolean contain = false;
                Evidence evidence = i.next();
                for(Alteration alteration : alterations) {
                    if(alteration != null) {
                        for(Alteration eviAlt : evidence.getAlterations()) {
                            if(eviAlt != null && alteration.equals(eviAlt)) {
                                contain = true;
                                break;
                            }
                        }
                        if(contain) {
                            i.remove();
                            break;
                        }
                    }
                }
            }
        }
        return evidences;
    }
}
