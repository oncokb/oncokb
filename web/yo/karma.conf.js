// Karma configuration
// Generated on Tue Jun 26 2018 16:08:01 GMT-0400 (EDT)

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'app/components/angular/angular.js',
            'app/components/angular-mocks/angular-mocks.js',
            'app/components/angular-chosen-localytics/dist/angular-chosen.js',
            'app/components/angular-recursion/angular-recursion.js',
            'app/components/angular-ui-sortable/sortable.js',
            'app/components/angular-animate/angular-animate.js',
            'app/components/angular-cookies/angular-cookies.js',
            'app/components/angular-resource/angular-resource.js',
            'app/components/angular-route/angular-route.js',
            'app/components/angular-sanitize/angular-sanitize.js',
            'app/components/angular-touch/angular-touch.js',
            'app/components/jquery/dist/jquery.js',
            'app/components/bootstrap/dist/js/bootstrap.js',
            'app/components/angular-bootstrap/ui-bootstrap.js',
            'app/components/angular-translate/angular-translate.js',
            'app/components/angular-dialog-service/dist/dialogs-default-translations.js',
            'app/components/angular-dialog-service/dist/dialogs.js',
            'app/components/x2js/xml2json.js',
            'app/components/angular-xml/angular-xml.js',
            'app/components/angular-contenteditable/angular-contenteditable.js',
            'app/components/datatables/media/js/jquery.js',
            'app/components/datatables/media/js/jquery.dataTables.js',
            'app/components/angular-datatables/dist/angular-datatables.js',
            'app/components/angular-datatables/dist/plugins/bootstrap/angular-datatables.bootstrap.js',
            'app/components/firebase/firebase.js',
            'app/components/angularfire/dist/angularfire.js',
            'app/components/underscore/underscore.js',
            'app/components/lodash/lodash.js',
            'app/scripts/**/*.js'
        ],


        // list of files / patterns to exclude
        exclude: [
            'app/scripts/app.js'
        ],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            'app/scripts/**/*.js': ['coverage']
        },

        coverageReporter: {
            type: 'html',
            dir: 'coverage/'
        },

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'coverage'],

        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['Chrome'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false,

        // Concurrency level
        // how many browser should be started simultaneous
        concurrency: Infinity
    })
};
