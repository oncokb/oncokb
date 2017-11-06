Repository for OncoKB, a precision oncology knowledge base.

We use Google Reatime API to store all information curators generated.
We use MySQL to store data after reviewing.

Current repository contains server-side and curation platform.
If you wish to deploy/modify OncoKB public website, please refer to OncoKB Public section.

# Front-end
OncoKB front-end is built with lots of great open source JS libraries. AngularJS is used as framework. Bower is used to manage denpendencies. Yeoman is used to initiate project and angular-generator is used to create angular directive/service/factory etc.

## Install project
1. Install npm & bower & yo & grunt-cli (globally)
2. Go to web/yo folder
3. npm install
4. bower install

## Prepare configuration files
1. Prepare files for front-end  
    ```
    cp -r web/yo/app/data-EXAMPLE web/yo/app/data
    ```
2. Prepare properties files  
    ```
    cp -r core/src/main/resources/properties-EXAMPLE core/src/main/resources/properties
    ```

## config.json setting
File is located under web/yo/app/data
```
{
    clientId: 'Your client ID from google developer console',
    scopes: [
        'https://www.googleapis.com/auth/plus.profile.emails.read',
        'https://www.googleapis.com/auth/drive.file'
    ],
    folderId: '', // The folder ID where you put all google realtime documents. By default, we will point you to an example folder.
    additionalsFolderId: '', // The folder ID where additional files are stored, including meta data file and curation queues file. By default, we will point you to an example folder.
    userRoles: {
        'public': 1, // 0001
        'user':   2, // 0010
        'curator':4, // 0100
        'admin':  8  // 1000
    },
    backupFolderId: '', //The backup folder ID. By default, we will point you to an example backup folder.
    users: '', // The google spreadsheet ID which used to manage the user info. Please share this file to the service email address with view permission. We will point you to an example file.
    curationLink: 'legacy-api/', // Your endpoints URL specifically designed for curation platform.
    apiLink: "legacy-api/",  // Your endpoints URL.
    privateApiLink: "api/private/", // Endpints are specifically designed to use internally.
    publicApiLink: "api/v1/",
    oncoTreeLink: 'http://oncotree.mskcc.org/oncotree/api/',
    testing: false // If the testing is set to ture, all endpoints will be disabled and will use the files from web/yo/app/data folder
};
```

## Properties file
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
    
## Profiles in web pom
* backend - core + API
* curate - core + API + curation website
* public - core + API + public website Please reference to OncoKB Public Website section.
    
## Use curation website without back-end
1. Set configuration 'testing' to true in config.json
2. In web/yo/app/data/oncokbInfo.json, add an element as below with your account information to the `users array`. 
```
{
    "name":"Your Name",
    "genes":null, 
    "role": 8, // follow the userRoles in config.json to specify role value
    "email":"your gmail address", // required
    "phases":null
}
```
3. Under web/yo/, run 'grunt serve'

## Coding Rules
Because of the similarity of the project, we follow jhipster requirement.
To ensure consistency throughout the source code, keep these rules in mind as you are working:

* All files must follow the [.editorconfig file](http://editorconfig.org/) located at the root of the project.
* Java files **must be** formatted using [Intellij IDEA's code style](http://confluence.jetbrains.com/display/IntelliJIDEA/Code+Style+and+Formatting).
* Web apps JavaScript files **must follow** [Google's JavaScript Style Guide](https://google-styleguide.googlecode.com/svn/trunk/javascriptguide.xml).
* AngularJS files **must follow** [John Papa's Angular 1 style guide] (https://github.com/johnpapa/angular-styleguide/blob/master/a1/README.md).

## OncoKB Public Website
In order to build a OncoKB public website instance, please clone [oncokb-public](https://github.com/oncokb/oncokb-public) to web/public folder. And in the pom file, please choose public as profile.

License
--------------------

OncoKB free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License, version 3, as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

A public instance of OncoKB (http://oncokb.org) is hosted and maintained by Memorial Sloan Kettering Cancer Center. It provides access to all curators in MSKCC knowledgebase team.

If you are interested in coordinating the development of new features, please contact team@oncokb.org.
