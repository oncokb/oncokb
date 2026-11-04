package org.mskcc.cbio.oncokb.service;

import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.oncokb.apiModels.CuratedGene;
import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.mskcc.cbio.oncokb.cache.CacheFetcher;
import org.mskcc.cbio.oncokb.controller.advice.ApiHttpErrorException;
import org.mskcc.cbio.oncokb.model.CancerGene;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.GeneUtils;
import org.mskcc.cbio.oncokb.util.MainUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mskcc.cbio.oncokb.util.HttpUtils.getDataDownloadResponseEntity;

/**
 * Cancer gene list and curated gene lookups shared by {@code UtilsApiController} and the MCP endpoint.
 * <p>
 * Methods return a {@link ResponseEntity} because a {@code version} request is served from the
 * archived data release, and the download helper reports a missing release through the status code.
 */
@Service
public class CancerGeneService {
    @Autowired
    CacheFetcher cacheFetcher;

    public ResponseEntity<List<CancerGene>> getCancerGeneList(String version) {
        if (version != null) {
            return getDataDownloadResponseEntity(version, FileName.CANCER_GENE_LIST, FileExtension.JSON);
        }
        List<CancerGene> result = this.cacheFetcher.getCancerGenes();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<List<CuratedGene>> getAllCuratedGenes(String version, Boolean includeEvidence, String hugoSymbol) throws ApiHttpErrorException {
        if (version != null) {
            // The symbol is resolved against current gene curation, so an archived version that
            // stored a since-renamed symbol would report a gene it actually contains as missing.
            if (StringUtils.isNotBlank(hugoSymbol) && !isLatestDataVersion(version)) {
                throw new ApiHttpErrorException("The hugoSymbol parameter is only supported for the latest data version (" + MainUtils.getDataVersion() + ").", HttpStatus.BAD_REQUEST);
            }
            ResponseEntity<List<CuratedGene>> archived = getDataDownloadResponseEntity(version, FileName.ALL_CURATED_GENES, FileExtension.JSON);
            if (StringUtils.isBlank(hugoSymbol) || !archived.getStatusCode().is2xxSuccessful() || archived.getBody() == null) {
                return archived;
            }
            return new ResponseEntity<>(filterCuratedGenesByHugoSymbol(archived.getBody(), hugoSymbol), archived.getStatusCode());
        }
        return new ResponseEntity<>(filterCuratedGenesByHugoSymbol(this.cacheFetcher.getCuratedGenesAll(includeEvidence), hugoSymbol), HttpStatus.OK);
    }

    private static boolean isLatestDataVersion(String version) {
        String latest = MainUtils.getDataVersion();
        return StringUtils.isNotBlank(latest) && normalizeDataVersion(latest).equals(normalizeDataVersion(version));
    }

    private static String normalizeDataVersion(String version) {
        return StringUtils.removeStartIgnoreCase(StringUtils.trimToEmpty(version), "v");
    }

    private List<CuratedGene> filterCuratedGenesByHugoSymbol(List<CuratedGene> genes, String hugoSymbol) throws ApiHttpErrorException {
        if (StringUtils.isBlank(hugoSymbol)) {
            return genes;
        }
        String trimmed = hugoSymbol.trim();
        Gene gene = GeneUtils.getGeneByHugoSymbol(trimmed);
        String queried = gene == null ? trimmed : gene.getHugoSymbol();
        List<Object> matches = new ArrayList<>();
        for (Object curatedGene : (List<?>) genes) {
            if (queried.equals(getCuratedGeneHugoSymbol(curatedGene))) {
                matches.add(curatedGene);
            }
        }
        if (matches.isEmpty()) {
            throw new ApiHttpErrorException("No curated gene found for hugoSymbol \"" + trimmed + "\".", HttpStatus.NOT_FOUND);
        }
        return asCuratedGenes(matches);
    }

    @SuppressWarnings("unchecked")
    private static List<CuratedGene> asCuratedGenes(List<?> genes) {
        return (List<CuratedGene>) genes;
    }

    private String getCuratedGeneHugoSymbol(Object curatedGene) {
        if (curatedGene instanceof CuratedGene) {
            return ((CuratedGene) curatedGene).getHugoSymbol();
        }
        if (curatedGene instanceof Map) {
            Object hugoSymbol = ((Map<?, ?>) curatedGene).get("hugoSymbol");
            return hugoSymbol == null ? null : hugoSymbol.toString();
        }
        return null;
    }
}
