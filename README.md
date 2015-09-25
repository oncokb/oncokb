Repository for OncoKB, an oncogenomics knowledgebase.

Multi-tier system for annotating and classifying mutation and copying number alteration events in cancer.

We use Google Reatime API to store all information curators generated.
We use MySQL to store data after reviewing

#Front-end
OncoKB front-end is built with lots of great open source JS libraries. AngularJS is used as framework. Bower is used to manage denpendencies. Yeoman is used to initiate project and angular-generator is used to create angular directive/service/factory etc.

##Install project
1. Install npm & bower & yo (globally)
2. Go to web/yo folder
3. npm install
4. bower install

##Use website without back-end
1. Copy data-EXAMPLE to data under web/yo/app/scripts/
2. Set parameter 'dataFromFile' to true in web/yo/app/scripts/services/dataconnector.js
3. Under web/yo/, run 'grunt serve'

## Show error in console
Uncomment the $delegate
`````````
$provide.decorator('$exceptionHandler', function($delegate, $injector){
    return function(exception, cause){
        var $rootScope = $injector.get('$rootScope');
        $rootScope.addError({message: 'Exception', reason: exception, case: cause});
        //$delegate(exception, cause);
    };
});
`````````

## app.js file setting (Will be moved to front end configure file)
```
OncoKB.config = {
    clientId: 'Your client ID from google developer console',
    scopes: [
        'https://www.googleapis.com/auth/plus.profile.emails.read',
        'https://www.googleapis.com/auth/drive.file'
    ],
    folderId: '', //The folder ID where you put all google realtime documents
    backupFolderId: '', //The backup folder ID
    userRoles: {
        'public': 1, // 0001
        'user':   2, // 0010
        'curator':4, // 0100
        'admin':  8  // 1000
    },
    users: '', //Your user management Google Spreadsheet ID, you need to share this file to the service email address. At least, give it view permission.
    accessLevels: {}
};
```

#Properties file
1. database.properties
    * jdbc.driverClassName : We use mysql as database. Here, it will be com.mysql.jdbc.Driver
    * jdbc.url: Databse : url
    * jdbc.username & jdbc.password: MySQL user name and password
2. config.properties
    * google.p_twelve : Your P12 private key path (You can generate this file from google developer console, more detials in Wiki)
    * google.service_account_email : Your service account email from google developer console.
    * google.username & google.password(Optional) : Google account info. It is used to send email
    * curation.log.email.to(Optional) : The email address where all emails will go


License
--------------------

OncoKB free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License, version 3, as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

A public instance of OncoKB (http://oncokb.org) is hosted and maintained by Memorial Sloan Kettering Cancer Center. It provides access to all curators in MSKCC knowledgebase team.

If you are interested in coordinating the development of new features, please contact oncokb@cbio.mskcc.org.
