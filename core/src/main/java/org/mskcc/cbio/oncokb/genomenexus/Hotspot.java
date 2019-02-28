/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal Genome Nexus.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.oncokb.genomenexus;

public class Hotspot
{
    private String id;

    private String hugoSymbol;

    private String transcriptId;

    private String residue;

    private Integer tumorCount;

    private String type;

    private Integer missenseCount;

    private Integer truncatingCount;

    private Integer inframeCount;

    private Integer spliceCount;

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public void setHugoSymbol(String hugoSymbol) {
        this.hugoSymbol = hugoSymbol;
    }

    public String getTranscriptId() {
        return this.transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }

    public Integer getTumorCount() {
        return tumorCount;
    }

    public void setTumorCount(Integer tumorCount) {
        this.tumorCount = tumorCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMissenseCount() {
        return missenseCount;
    }

    public void setMissenseCount(Integer missenseCount) {
        this.missenseCount = missenseCount;
    }

    public Integer getTruncatingCount() {
        return truncatingCount;
    }

    public void setTruncatingCount(Integer truncatingCount) {
        this.truncatingCount = truncatingCount;
    }

    public Integer getInframeCount() {
        return inframeCount;
    }

    public void setInframeCount(Integer inframeCount) {
        this.inframeCount = inframeCount;
    }

    public Integer getSpliceCount() {
        return spliceCount;
    }

    public void setSpliceCount(Integer spliceCount) {
        this.spliceCount = spliceCount;
    }

    @Override
    public int hashCode() {
        return (hugoSymbol + residue + type + tumorCount).hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Hotspot) {
            return this.hashCode() == obj.hashCode();
        }
        else {
            return super.equals(obj);
        }
    }
}
