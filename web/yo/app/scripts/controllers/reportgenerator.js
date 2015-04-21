'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:ReportgeneratorCtrl
 * @description
 * # ReportgeneratorCtrl
 * Controller of the oncokb
 */
angular.module('oncokbApp')
    .controller('ReportgeneratorCtrl',
    function($scope, FileUploader, dialogs) {
        function initUploader() {
            var uploader = $scope.uploader = new FileUploader();

            uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
                console.info('onWhenAddingFileFailed', item, filter, options);
            };

            uploader.onAfterAddingFile  = function(fileItem) {
                console.info('onAfterAddingFile', fileItem);
                $scope.status = {
                    fileSelected: false,
                    isXLSX: false,
                    isXML: false
                };
                $scope.status.fileSelected = true;
                $scope.fileItem = fileItem;

                if (fileItem.file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet') {
                    $scope.status.isXLSX = true;
                }else if (fileItem.file.type === 'text/xml') {
                    $scope.status.isXML = true;
                }else {
                    dialogs.error('Error', 'Do not support the type of selected file, only XLSX or XML file is supported.');
                    uploader.removeFromQueue(fileItem);
                }

                console.log($scope.status);
            };

            uploader.onAfterAddingAll = function(addedFileItems) {
                console.info('onAfterAddingAll', addedFileItems);
            };
            uploader.onBeforeUploadItem = function(item) {
                console.info('onBeforeUploadItem', item);
            };
            uploader.onProgressItem = function(fileItem, progress) {
                console.info('onProgressItem', fileItem, progress);
            };
            uploader.onProgressAll = function(progress) {
                console.info('onProgressAll', progress);
            };
            uploader.onSuccessItem = function(fileItem, response, status, headers) {
                console.info('onSuccessItem', fileItem, response, status, headers);
            };
            uploader.onErrorItem = function(fileItem, response, status, headers) {
                console.info('onErrorItem', fileItem, response, status, headers);
            };
            uploader.onCancelItem = function(fileItem, response, status, headers) {
                console.info('onCancelItem', fileItem, response, status, headers);
            };
            uploader.onCompleteItem = function(fileItem, response, status, headers) {
                console.info('onCompleteItem', fileItem, response, status, headers);
            };
            uploader.onCompleteAll = function() {
                console.info('onCompleteAll');
            };
        }

        $scope.init = function(){
            $scope.status = {
                fileSelected: false,
                isXLSX: false,
                isXML: false
            };
            initUploader();
        };
    });
