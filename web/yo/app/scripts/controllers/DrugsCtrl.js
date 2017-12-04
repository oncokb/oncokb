'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$scope', '$rootScope', '$location', '$timeout',
        '$routeParams', '_', 'config', 'importer', 'storage', 'documents',
        'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector',
        'OncoKB', 'stringUtils', 'S', 'mainUtils', 'gapi', 'UUIDjs', 'dialogs',
        function($scope, $rootScope, $location, $timeout, $routeParams, _,
                 config, importer, storage, Documents, users,
                 DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector,
                 OncoKB, stringUtils, S, MainUtils, gapi, UUIDjs, dialogs) {

            $scope.userRole = users.getMe().role;

            function getAllDrugs() {
                $scope.rendering = true;
                DatabaseConnector.getAllDrugs(function(data) {
                    $scope.allDrugs = data;
                    $scope.rendering = false;
                }, function(error) {
                    console.log('error happened when loading all drugs information', error);
                    // var subject = 'Get drugs data failed from database';
                    // var content = 'Error happened when loading Drugs page. The system error returned is ' + error;
                    // MainUtils.sendEmail('dev.oncokb@gmail.com', subject, content);
                });
            }

            $scope.init = function () {
                getAllDrugs();
            };



        }]
    );
