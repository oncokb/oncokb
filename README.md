Repository for OncoKB, a precision oncology knowledge base.

We use Google Reatime API to store all information curators generated.
We use MySQL to store data after reviewing.

Currant repository contains server-side and curation platform.
If you wish to deploy/modify OncoKB public website, please refer to OncoKB Public section.

#Front-end
OncoKB front-end is built with lots of great open source JS libraries. AngularJS is used as framework. Bower is used to manage denpendencies. Yeoman is used to initiate project and angular-generator is used to create angular directive/service/factory etc.

##Install project
1. Install npm & bower & yo & grunt-cli (globally)
2. Go to web/yo folder
3. npm install
4. bower install

##Use website without back-end
1. Copy data-EXAMPLE to data under web/yo/app/
2. Prepare properties files  
    ```
    cd core/src/main/resources/properties/
    cp config-EXAMPLE.properties config.properties
    cp database-EXAMPLE.properties database.properties
    cp log4j-EXAMPLE.properties log4j.properties
    ```

3. Set configuration 'testing' to true in config.json
4. Under web/yo/, run 'grunt serve'

## Show error in console
Uncomment the $delegate
```
$provide.decorator('$exceptionHandler', function($delegate, $injector){
    return function(exception, cause){
        var $rootScope = $injector.get('$rootScope');
        $rootScope.addError({message: 'Exception', reason: exception, case: cause});
        //$delegate(exception, cause);
    };
});
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
    backupFolderId: '', //The backup folder ID. By default, we will point you to an example backup folder.
    userRoles: {
        'public': 1, // 0001
        'user':   2, // 0010
        'curator':4, // 0100
        'admin':  8  // 1000
    },
    users: '', // The google spreadsheet ID which used to manage the user info. Please share this file to the service email address with view permission.
    curationLink: 'legacy-api/', // Your endpoints URL specifically designed for curation platform.
    apiLink: "legacy-api/",  // Your endpoints URL.
    oncoTreeLink: 'http://oncotree.mskcc.org/oncotree/api/',
    testing: false // If the testing is set to ture, all endpoints will be disabled and will use the files from web/yo/app/data folder
};
```

##Properties file
1. database.properties
    * jdbc.driverClassName : We use mysql as database. Here, it will be com.mysql.jdbc.Driver
    * jdbc.url: Database url
    * jdbc.username & jdbc.password: MySQL user name and password
2. config.properties
    * google.p_twelve : Your P12 private key path (You can generate this file from google developer console, more detials in Wiki)
    * google.service_account_email : Your service account email from google developer console.
    * google.username & google.password(Optional) : Google account info. It is used to send email
    * data.version & data.version_date(Optional) : These two properties will be attached to API call.
    * springfox.documentation.swagger.v2.path : Swagger.json path. Default: /api-docs
    * cancerhotspots.single : [Cancer hotspots service](http://cancerhotspots.org). Default: http://cancerhotspots.org/api/hotspots/single

##Coding Rules
Because of the similarity of the project, we follow jhipster requirement.
To ensure consistency throughout the source code, keep these rules in mind as you are working:

* All files must follow the [.editorconfig file](http://editorconfig.org/) located at the root of the project.
* Java files **must be** formatted using [Intellij IDEA's code style](http://confluence.jetbrains.com/display/IntelliJIDEA/Code+Style+and+Formatting).
* Web apps JavaScript files **must follow** [Google's JavaScript Style Guide](https://google-styleguide.googlecode.com/svn/trunk/javascriptguide.xml).
* AngularJS files **must follow** [John Papa's Angular 1 style guide] (https://github.com/johnpapa/angular-styleguide/blob/master/a1/README.md).

##OncoKB Public Website
In order to build a OncoKB public website instance, please clone [oncokb-public](https://github.com/knowledgesystems/oncokb-public) to web/public folder. And in the pom file, please choose public as profile.

License
--------------------

OncoKB free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License, version 3, as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

A public instance of OncoKB (http://oncokb.org) is hosted and maintained by Memorial Sloan Kettering Cancer Center. It provides access to all curators in MSKCC knowledgebase team.

If you are interested in coordinating the development of new features, please contact oncokb@cbio.mskcc.org.
