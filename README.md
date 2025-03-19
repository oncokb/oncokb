# OncoKB Core

Repository for OncoKB, a precision oncology knowledge base.

The core of OncoKB Annotation service.

## Status

[![Application CI](https://github.com/oncokb/oncokb/workflows/Application%20CI/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Application+CI%22) [![Unit Tests](https://github.com/zhx828/oncokb/workflows/Unit%20Tests/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Unit+Tests%22) [![Release Management](https://github.com/oncokb/oncokb/workflows/Release%20Management/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A"Release+Management") [![Sentrey Release](https://github.com/oncokb/oncokb/workflows/Sentrey%20Release/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Sentrey+Release%22)

## Info

<a href="https://ascopubs.org/doi/full/10.1200/PO.17.00011"><img src="https://img.shields.io/badge/DOI-10.1200%2FPO.17.00011-1c75cd" /></a>

## Running Environment

Please confirm your running environment is:

-   **Java version: 8**
-   **MySQL version: 8.0.36**

## Prepare properties files

```
cp -r core/src/main/resources/properties-EXAMPLE core/src/main/resources/properties
```

### Properties file

1. database.properties
    - jdbc.driverClassName : We use mysql as database. Here, it will be com.mysql.jdbc.Driver
    - jdbc.url: Database url
    - jdbc.username & jdbc.password: MySQL user name and password
2. config.properties
    - oncotree.api: [OncoTree service](http://oncotree.info/). Default: http://oncotree.info/api/

## Build the WAR file

### Choose profile

1. `enterprise`: includes public and private APIs
2. `public-api`: includes only public API endpoints
3. `image-build`: uses Jib to generate a docker image and push to DockerHub

> **_NOTE:_**  We deprecated the legacy `public` and `curate` profiles.

`mvn clean install -P <profile(s)> -DskipTests=true`

The WAR file is under `/web/target/`

## Run locally on VSCode

1. Download VSCode extension `Community Server Connector`
2. In VSCode sidebar, find `Servers` dropdown.
3. Right click `Community Server Connector` and choose `Create New Server`.
4. Either download Tomcat 8 on local machine or let CSC download for you.
5. Right click Tomcat server and choose `Add Deployment`. This is the WAR file generated in the previous step.
6. Start Tomcat server. Make sure to `Publish Server (Full)` to keep server synchronized with WAR file (if changes were made).

## Run with Docker containers

OncoKB™ is a precision oncology knowledge base developed at Memorial Sloan Kettering Cancer Center that contains biological and clinical information about genomic alterations in cancer. OncoKB uses [Genome Nexus](https://github.com/genome-nexus/genome-nexus) to annotate genomic change to protein change using OncoKB picked transcripts. By default, the API requests are sent to www.genomenexus.org for GRCh37 and grch38.genomenexus.org for GRCh38. However, you can choose to use a local version of Genome Nexus by following the instructions for [**Option A**](#option-a-with-local-installation-of-genome-nexus), otherwise follow instructions for [**Option B**](#option-b-without-local-installation-of-genome-nexus).

OncoKB docker compose file consists of the following services:

-   OncoKB: provides variant annotations
-   OncoKB Transcript: serves OncoKB metadata including gene, transcript, sequence, etc.
-   Genome Nexus: provides annotation and interpretation of genetic variants in cancer

    -   GRCh37 (optional):
        -   gn-spring-boot: the backend service responsible for aggregating variant annotations from various sources
        -   gn-mongo: variants fetched from external resources and small static data are cached in the MongoDB database
        -   gn-vep: is a spring boot REST wrapper service for [VEP](https://github.com/Ensembl/ensembl-vep) using GRCh37 data
    -   GRCh38 (optional):
        -   gn-spring-boot-grch38: same as `gn-spring-boot` service, however the VEP URL points to `gn-vep-grch38`
        -   gn-mongo-grch38: contains static data relevant to GRCh38
        -   gn-vep-grch38: a spring boot REST wrapper service for VEP using GRCh38 data

### Option A: With Local installation of Genome Nexus

For this option, you need to download the VEP cache, which is used in the `gn-vep` and `gn-vep-grch38` services. We have pre-downloaded the VEP data and saved them to our AWS S3 Bucket. If interested, here are the instructions we followed to [download the Genome Nexus VEP Cache](https://github.com/genome-nexus/genome-nexus-vep/blob/master/README.md#create-vep-cache).

1. OncoKB requires a MySQL server and the `oncokb` and `oncokb-transcript` databases imported. This step must be completed before continuing the installation process. Reach out to contact@oncokb.org to get access to the data dump. [How to setup MySQL Server](#how-to-setup-mysql-server)

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
4. Run docker-compose to create containers.
    ```
    docker-compose --profile genome-nexus up -d
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

#### Generating new VEP data

OncoKB predownloads VEP data and saves it to AWS S3 bucket. These steps are for OncoKB developers and show how to download and upload new Ensembl VEP data to S3. However, you can follow along and
save VEP data to your own S3 bucket.

1. Change Ensembl image in genome-nexus-vep [Dockerfile](https://github.com/genome-nexus/genome-nexus-vep/blob/8479402d4f9db8236ab297f7cea6aada43f98574/Dockerfile#L7) to desired version
1. [Follow instructions](https://github.com/genome-nexus/genome-nexus-vep/blob/master/README.md#create-vep-cache) to download VEP cache files and FASTA files for GRCh37 and GRCh38.
1. After downloading your directory should like:

```
VEP_CACHE/
├─ homo_sapiens/
│  ├─ 98_GRCh37/
│  ├─ 98_GRCh38/
```

3. Zip the files

```
tar cf 98_GRCh37.tar homo_sapiens/98_GRCh37
tar cf 98_GRCh38.tar homo_sapiens/98_GRCh38
```

4. Go to AWS S3 webpage and under `oncokb/gn-vep-data/`, create two folders:

```
98_GRCh37/
98_GRCh38/
```

5. Upload `tar` files to corresponding S3 folders
6. Make the two S3 folders (`oncokb/gn-vep-data/98_GRCh37/` and `oncokb/gn-vep-data/98_GRCh38/`) publicly accessible
7. Update `gn-vep` and `gn-vep-grch38` services in `docker-compose.yml`

```
Modify environment variable to point to the new FASTA file

gn-vep
VEP_FASTAFILERELATIVEPATH=homo_sapiens/98_GRCh37/Homo_sapiens.GRCh37.75.dna.primary_assembly.fa.gz

gn-vep-grch38
VEP_FASTAFILERELATIVEPATH=homo_sapiens/98_GRCh38/Homo_sapiens.GRCh38.dna.toplevel.fa.gz
```

7. Modify [Dockerfile line](https://github.com/genome-nexus/genome-nexus-vep/blob/master/Dockerfile#LL7C2-L7C2) in [genome-nexus-vep](https://github.com/genome-nexus/genome-nexus-vep) to use the new Ensembl VEP image. As of 4/28/2023, genome-nexus-vep uses `ensemblorg/ensembl-vep:release_98.3`.
8. Push new genome-nexus-vep image to DockerHub
9. Change the image for both `gn-vep` and `gn-vep-grch38` to the image built in step 7.

## How to setup MySQL Server

### Option A: MySQL Docker Container

1. Create a Docker network
   `docker network create mysql-test-network`

2. Run MySQL container and attach to the network
   `docker run --name my-mysql -e MYSQL_ROOT_PASSWORD=root -p 3307:3306 --network mysql-test-network -d mysql:8`
   We suggest using MySQL 8 since that is what we use in production. Our README still says 5.7.28, but we will update that shortly.

3. If you already have a MySQL Docker container running, follow the steps to add network (otherwise skip)

```
docker stop my-mysql
docker network connect mysql-test-network my-mysql
docker start my-mysql
```

4. Update docker-compose file

```
# Add networks to each service
services:
    oncokb:
        networks:
            - mysql-test-network
        ...
    oncokb-transcript:
        networks:
            - mysql-test-network
        ...
# Declare external network that MySQL container is connected to
networks:
  mysql-test-network:
    external: true
```

5. Now, the container is connected to the `mysql-test-network` network, and other containers on the same network can communicate with it using its container name (my-mysql).

```
#JDBC URL
jdbc:mysql://my-mysql:3306/oncokb_core?useUnicode=yes&characterEncoding=UTF-8&useSSL=false
```

### Option B: Local MySQL Server

1. Follow MySQL [documentation](https://dev.mysql.com/doc/mysql-installation-excerpt/8.0/en/installing.html)

2. On MacOS and Windows, you can use [host.docker.internal](https://docs.docker.com/desktop/networking/#i-want-to-connect-from-a-container-to-a-service-on-the-host) to point docker containers to MySQL server running on local machine.

```
#JDBC URL
jdbc:mysql://host.docker.internal:3306/oncokb_core?useUnicode=yes&characterEncoding=UTF-8&useSSL=false
```

## Questions?

The best way is to send an email to contact@oncokb.org so all our team members can help.
