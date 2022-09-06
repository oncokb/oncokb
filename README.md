# OncoKB Core
Repository for OncoKB, a precision oncology knowledge base.

The core of OncoKB Annotation service.

## Status

[![Application CI](https://github.com/oncokb/oncokb/workflows/Application%20CI/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Application+CI%22) [![Unit Tests](https://github.com/zhx828/oncokb/workflows/Unit%20Tests/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Unit+Tests%22) [![Release Management](https://github.com/oncokb/oncokb/workflows/Release%20Management/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A"Release+Management") [![Sentrey Release](https://github.com/oncokb/oncokb/workflows/Sentrey%20Release/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Sentrey+Release%22) 

## Info

<a href="https://ascopubs.org/doi/full/10.1200/PO.17.00011"><img src="https://img.shields.io/badge/DOI-10.1200%2FPO.17.00011-1c75cd" /></a>

## Running Environment
Please confirm your running environment is:
* **Java version: 8**
* **MySQL version: 5.7.28**


## Prepare properties files  
```
cp -r core/src/main/resources/properties-EXAMPLE core/src/main/resources/properties
```

### Properties file
1. database.properties
    * jdbc.driverClassName : We use mysql as database. Here, it will be com.mysql.jdbc.Driver
    * jdbc.url: Database url
    * jdbc.username & jdbc.password: MySQL user name and password
2. config.properties
    * oncotree.api: [OncoTree service](http://oncotree.info/). Default: http://oncotree.info/api/
    * google.username & google.password(Optional) : Google account info. It is used to send email
    
    
## Build the WAR file
`mvn clean install -P public -DskipTests=true`

The WAR file is under `/web/target/`

## Deploy with frontend
Please choose one of the profile when building the war file
* curate - core + API + curation website
* public - core + API + public website (deprecated)

You could find specific instructions in curate or public repo,

## Run with Docker containers
OncoKB™ is a precision oncology knowledge base developed at Memorial Sloan Kettering Cancer Center that contains biological and clinical information about genomic alterations in cancer. OncoKB uses [Genome Nexus](https://github.com/genome-nexus/genome-nexus) to annotate genomic change to protein change using OncoKB picked transcripts. By default, the API requests are sent to www.genomenexus.org for GRCh37 and grch38.genomenexus.org for GRCh38. However, you can choose to use a local version of Genome Nexus by following the instructions for [**Option A**](#option-a-with-local-installation-of-genome-nexus), otherwise follow instructions for [**Option B**](#option-b-without-local-installation-of-genome-nexus).

OncoKB docker compose file consists of the following services:
- OncoKB: provides variant annotations
- OncoKB Transcript: serves OncoKB metadata including gene, transcript, sequence, etc.
- Genome Nexus: provides annotation and interpretation of genetic variants in cancer

    - GRCh37 (optional):
        - gn-spring-boot: the backend service responsible for aggregating variant annotations from various sources
        - gn-mongo: variants fetched from external resources and small static data are cached in the MongoDB database
        - gn-vep: is a spring boot REST wrapper service for [VEP](https://github.com/Ensembl/ensembl-vep) using GRCh37 data
    - GRCh38 (optional):
        - gn-spring-boot-grch38: same as `gn-spring-boot` service, however the VEP URL points to `gn-vep-grch38`
        - gn-mongo-grch38: contains static data relevant to GRCh38
        - gn-vep-grch38: a spring boot REST wrapper service for VEP using GRCh38 data


### Option A: With Local installation of Genome Nexus

For this option, you need to download the VEP cache, which is used in the `gn-vep` and `gn-vep-grch38` services. We have pre-downloaded the VEP data and saved them to our AWS S3 Bucket. If interested, here are the instructions we followed to [download the Genome Nexus VEP Cache](https://github.com/genome-nexus/genome-nexus-vep/blob/master/README.md#create-vep-cache).

1. OncoKB requires a MySQL server and the `oncokb` and `oncokb-transcript` databases imported. This step must be completed before continuing the installation process. Reach out to contact@oncokb.org to get access to the data dump. 

2. Download the Genome Nexus VEP data from our AWS S3 Bucket.
    ```
    # The home directory is used to store the VEP cache in this tutorial, but this can be changed to your preferred download location.
    cd ~
    mkdir gn-vep-data && cd "$_"

    mkdir 98_GRCh37 && cd "$_"
    curl -o 98_GRCh37.tar https://oncokb.s3.amazonaws.com/gn-vep-data/98_GRCh37/98_GRCh37.tar
    tar xvf 98_GRCh37.tar

    cd ..
    mkdir 98_GRCh38 && cd "$_"
    curl -o 98_GRCh38.tar https://oncokb.s3.amazonaws.com/gn-vep-data/98_GRCh38/98_GRCh38.tar
    tar xvf 98_GRCh38.tar
    ```
3. Set environment variable for the location of VEP caches
    ```
    # Update path if the VEP data was installed elsewhere
    export VEP_CACHE=~/gn-vep-data/98_GRCh37
    export VEP_GRCH38_CACHE=~/gn-vep-data/98_GRCh38
    ```
4. Run docker-compose to build images and create containers.
    ```
    docker-compose --profile genome-nexus up -d --build
    ```
    **Note:** The --profile argument is used as a way to selectively enable services. Services with the genome-nexus profile will only be spun up when the profile is specified.

### Option B: Without local installation of Genome Nexus
1. OncoKB requires a MySQL server and the `oncokb` and `oncokb-transcript` databases imported. This step must be completed before continuing the installation process. Reach out to contact@oncokb.org to get access to the data dump. 
2. Remove `-Dgenome_nexus.grch37.url` and `-Dgenome_nexus.grch38.url` properties from the `oncokb` service.
3. Run docker-compose to spin up oncokb and oncokb-transcript services
    ```
    docker-compose up -d
    ```

### Additional Information

#### Generating oncokb-transcript token

The docker compose file has a pre-generated oncokb-transcript [JWT](https://jwt.io/introduction) token, which is required to make API requests to the oncokb-transcript service. To generate the JWT token, go to the https://jwt.io/ website and follow these instructions:

1. Add the `auth` key and set it to `ROLE_ADMIN` to grant roles. The `payload` section should look something like this:
    ```
    {
        "sub": "1234567890",
        "name": "John Doe",
        "auth":"ROLE_ADMIN",
        "iat": 1516239022
    }
    ```
2. In the `Verify Signature` section, check the box `secret base64 encoded`. Copy and paste the [oncokb-transcript base64 secret](https://github.com/oncokb/oncokb-transcript/blob/master/src/main/resources/config/application-prod.yml#L106) into the input box.
    - You can also change the default base64 secret used for encoding by generating a base64 string and add the environment variable, `JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET: <new-base64-string>`, to oncokb-transcript.
3. Replace `-Doncokb_transcript.token` with the JWT token you generated.

## Questions?
The best way is to send an email to contact@oncokb.org so all our team members can help.
