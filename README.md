Repository for OncoKB, a precision oncology knowledge base.

The core of OncoKB Annotation service.

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
    
    
## Build the war file
`mvn clean install -P public -DskipTests=true`

## Deploy with frontend
Please choose one of the profile when building the war file
* curate - core + API + curation website
* public - core + API + public website
         

License
--------------------

OncoKB free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License, version 3, as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

A public instance of OncoKB (https://www.oncokb.org) is hosted and maintained by Memorial Sloan Kettering Cancer Center. It provides access to all curators in MSKCC knowledgebase team.

If you are interested in coordinating the development of new features, please contact contact@oncokb.org.
