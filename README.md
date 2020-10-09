# OncoKB Core
Repository for OncoKB, a precision oncology knowledge base.

The core of OncoKB Annotation service.

## Status

[![Build Status](https://travis-ci.org/oncokb/oncokb.svg?branch=master)](https://travis-ci.org/oncokb/oncokb) ![Unit Tests](https://github.com/zhx828/oncokb/workflows/Unit%20Tests/badge.svg) [![Release Management](https://github.com/oncokb/oncokb/workflows/Release%20Management/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A"Release+Management") [![Sentrey Release](https://github.com/oncokb/oncokb/workflows/Sentrey%20Release/badge.svg)](https://github.com/oncokb/oncokb/actions?query=workflow%3A%22Sentrey+Release%22) 

## Info

[![Gitter](https://img.shields.io/gitter/room/oncokb/public-chat)](https://gitter.im/oncokb/public-chat) <a href="https://ascopubs.org/doi/full/10.1200/PO.17.00011"><img src="https://img.shields.io/badge/DOI-10.1200%2FPO.17.00011-1c75cd" /></a>

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
    * google.p_twelve : Your P12 private key path (You can generate this file from google developer console, more detials in Wiki)
    * google.service_account_email : Your service account email from google developer console.
    * cancerhotspots.single : [Cancer hotspots service](http://cancerhotspots.org). Default: http://cancerhotspots.org/api/hotspots/single
    * oncotree.api: [OncoTree service](http://oncotree.mskcc.org/oncotree/). Default: http://oncotree.mskcc.org/oncotree/api/
    * google.username & google.password(Optional) : Google account info. It is used to send email
    * data.version & data.version_date(Optional) : These two properties will be attached to API call.
    
    
## Build the WAR file
`mvn clean install -P public -DskipTests=true`

The WAR file is under `/web/target/`

## Deploy with frontend
Please choose one of the profile when building the war file
* curate - core + API + curation website
* public - core + API + public website (deprecated)

You could find specific instructions in curate or public repo,
         
## Questions?
The best way is to send an email to contact@oncokb.org so all our team members can help.
