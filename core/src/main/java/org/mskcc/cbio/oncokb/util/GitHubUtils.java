package org.mskcc.cbio.oncokb.util;

import com.sun.mail.iap.ByteArray;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHBlob;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mskcc.cbio.oncokb.apiModels.download.DownloadAvailability;
import org.mskcc.cbio.oncokb.apiModels.download.FileExtension;
import org.mskcc.cbio.oncokb.apiModels.download.FileName;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mskcc.cbio.oncokb.util.FileUtils.getFileName;

/**
 * Created by Hongxin Zhang on 10/21/19.
 */
public class GitHubUtils {
    private final static String ONCOKB_DATA_ACCESS_TOKEN_PROPERTY_NAME = "oncokb_data.access_token";
    private final static String ONCOKB_DATA_REPO = "knowledgesystems/oncokb-data/contents";

    public static String getOncoKBData(String version, String fileName) throws HttpClientErrorException, IOException, NoPropertyException {
        GHBlob ghBlob = getGHBlob(version, fileName);
        return IOUtils.toString(ghBlob.read(), StandardCharsets.UTF_8);
    }

    public static byte[] getOncoKBDataInBytes(String version, String fileName) throws HttpClientErrorException, IOException, NoPropertyException {
        GHBlob ghBlob = getGHBlob(version, fileName);
        return IOUtils.toByteArray(ghBlob.read());
    }

    private static GHBlob getGHBlob(String version, String fileName) throws IOException, NoPropertyException {
        GHRepository repo = getOncoKBDataRepo();
        List<GHContent> contents = new ArrayList<>();
        try {
            contents = repo.getDirectoryContent("/RELEASE/" + version);
        } catch (Exception e) {
            // in this case, is the directory is not available.
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }

        Optional<GHContent> matchedContent = contents.stream().filter(content -> content.getName().equals(fileName)).findFirst();
        if (matchedContent.isPresent()) {
            String sha = matchedContent.get().getSha();
            GHBlob ghBlob = repo.getBlob(sha);
            return ghBlob;
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }

    public static String getOncoKBSqlDumpFileName(String version) {
        if (version == null) {
            return "";
        }
        return "oncokb_" + version.replace(".", "_") + ".sql" + FileExtension.GZ.getExtension();
    }

    public static String getOncoKBTranscriptSqlDumpFileName(String version) {
        if (version == null) {
            return "";
        }
        return "oncokb_transcript_" + version.replace(".", "_") + ".sql" + FileExtension.GZ.getExtension();
    }

    private static Boolean checkFileNameExists(List<GHContent> files, String fileName) {
        return checkFileNameExists(files, fileName, false);
    }

    private static Boolean checkFileNameExists(List<GHContent> files, String fileName, boolean ignoreCase) {
        return files
            .stream()
            .filter(file -> ignoreCase ? file.getName().equalsIgnoreCase(fileName) : file.getName().equals(fileName))
            .findFirst()
            .isPresent();
    }

    private static Boolean checkSqlDumpExists(List<GHContent> files, String version) {
        return files
            .stream()
            .filter(file -> file.getName().startsWith(getOncoKBSqlDumpFileName(version)))
            .findFirst()
            .isPresent();
    }

    public static List<DownloadAvailability> getDownloadAvailability() throws HttpClientErrorException, IOException, NoPropertyException {
        List<DownloadAvailability> downloadAvailabilities = new ArrayList<>();
        GHRepository repo = getOncoKBDataRepo();
        List<GHContent> versions = repo.getDirectoryContent("/RELEASE/");
        for (GHContent version : versions) {
            List<GHContent> files = new ArrayList<>();
            try {
                files = repo.getDirectoryContent("/RELEASE/" + version.getName());
            } catch (Exception e) {
                // Preventing the item is not a directory, but we want to continue indexing the items.
                continue;
            }
            DownloadAvailability downloadAvailability = new DownloadAvailability(version.getName());

            if (checkFileNameExists(files, getFileName(FileName.ALL_ANNOTATED_VARIANTS, FileExtension.TEXT))) {
                downloadAvailability.setHasAllAnnotatedVariants(true);
            }
            if (checkFileNameExists(files, getFileName(FileName.ALL_ACTIONABLE_VARIANTS, FileExtension.TEXT))) {
                downloadAvailability.setHasAllActionableVariants(true);
            }
            if (checkFileNameExists(files, getFileName(FileName.ALL_CURATED_GENES, FileExtension.TEXT))) {
                downloadAvailability.setHasAllCuratedGenes(true);
            }
            if (checkFileNameExists(files, getFileName(FileName.CANCER_GENE_LIST, FileExtension.TEXT))) {
                downloadAvailability.setHasCancerGeneList(true);
            }
            if (checkFileNameExists(files, getFileName(FileName.README, FileExtension.MARK_DOWN), true)) {
                downloadAvailability.setHasReadme(true);
            }
            if (checkSqlDumpExists(files, version.getName())) {
                downloadAvailability.setHasSqlDump(true);
            }
            downloadAvailabilities.add(downloadAvailability);
        }
        return downloadAvailabilities;
    }

    private static GitHub getGitHubConnection() throws IOException, NoPropertyException {
        String token = PropertiesUtils.getProperties(ONCOKB_DATA_ACCESS_TOKEN_PROPERTY_NAME);
        if (token == null) {
            throw new NoPropertyException("The data access token is not available.");
        }
        return GitHub.connectUsingOAuth(token);
    }

    private static GHRepository getOncoKBDataRepo() throws IOException, NoPropertyException {
        GitHub github = getGitHubConnection();
        return github.getRepository(ONCOKB_DATA_REPO);
    }
}
