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

**OncoKB Core** requires that a MySQL server is started and has the the oncokb data imported into the database. Reach out to contact@oncokb.org to get access to the data dump.

To run local version of OncoKB Core without a local installation of Genome Nexus, follow the steps:
1. Start local MySQL server with imported data
2. Update oncokb core properties
    * `-Djdbc.url`: MySQL database url
    * `-Djdbc.username` and `-Djdbc.password`: MySQL server username and password
    * `-Dgenome_nexus.grch37.url`: Change to https://www.genomenexus.org
    * `-Dgenome_nexus.grch38.url`: Change to https://grch38.genomenexus.org

3. Run docker-compose to pull oncokb docker image and create container
    ```
    docker-compose up -d
    ```

<br>

**Genome Nexus** requires the following services:
- Genome Nexus [Spring Boot](https://github.com/genome-nexus/genome-nexus) application
- Local installation of [genome-nexus-vep](https://github.com/genome-nexus/genome-nexus-vep)
- MongoDB database, using [genome-nexus-importer](https://github.com/genome-nexus/genome-nexus-importer) to setup static data

1. Follow documentation on [downloading the Genome Nexus VEP Cache](https://github.com/genome-nexus/genome-nexus-vep/blob/master/README.md#create-vep-cache) for `GRCh37` and `GRCh38`.
    ```
    # Set location of GRCh37 local cache
    export VEP_CACHE=

    # Set location of GRCh38 local cache
    export VEP_GRCH38_CACHE=
    ```

2. Run docker-compose to build images and create containers:
    ```
    docker-compose --profile genome-nexus up -d --build
    ```

Stop and remove containers:
```
docker-compose down
```

## Questions?
The best way is to send an email to contact@oncokb.org so all our team members can help.
