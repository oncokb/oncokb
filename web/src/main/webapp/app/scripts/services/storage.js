'use strict';

/**
 * @ngdoc service
 * @name oncokb.storage
 * @description
 * # storage
 * Service in the oncokb.
 */
angular.module('oncokb')
  .service('storage', ['$q', '$rootScope', 'config', 'gapi',
  /**
   * Handles document creation & loading for the app. Keeps only
   * one document loaded at a time.
   *
   * @param $q
   * @param $rootScope
   * @param config
   */
  function ($q, $rootScope, config, gapi) {
    this.id = null;
    this.document = null;

    /**
     * Close the current document.
     */
    this.closeDocument = function () {
      this.document.close();
      this.document = null;
      this.id = null;
    };

    /**
     * Ensure the document is loaded.
     *
     * @param id
     * @returns {angular.$q.promise}
     */
    this.getDocument = function (id) {
        var deferred = $q.defer();
        var onComplete = function (result) {
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
            $rootScope.$digest();
        };
        gapi.client.load('drive', 'v2', function() {
            gapi.client.drive.files.get({
              'fileId' : id
            }).execute(onComplete);
        });
        return deferred.promise;
    }

    /**
     * Retrieve a list of File resources.
     *
     * @param {Function} callback Function to call when the request is complete.
     */
    this.retrieveAllFiles = function() {
        var retrievePageOfFiles = function(request, result) {
            request.execute(function(resp) {
              result = result.concat(resp.items);
              console.log(result);
              var nextPageToken = resp.nextPageToken;
              if (nextPageToken) {
                request = gapi.client.drive.files.list({
                  'pageToken': nextPageToken
                });
                retrievePageOfFiles(request, result);
              } else {
                deferred.resolve(result);
              }
            });
        }
        
        var deferred = $q.defer();
        gapi.client.load('drive', 'v2', function() {
            var initialRequest = gapi.client.drive.files.list();
            retrievePageOfFiles(initialRequest, []);
            return deferred.promise;
        });
    }

    this.downloadFile = function(file) {
        var deferred = $q.defer();
        var onComplete = function (result) {
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
        };
        if (file.exportLinks['text/html']) {
            var accessToken = gapi.auth.getToken().access_token;
            var xhr = new XMLHttpRequest();
            xhr.open('GET', file.exportLinks['text/html']);
            xhr.setRequestHeader('Authorization', 'Bearer ' + accessToken);
            xhr.onload = function() {
                onComplete(xhr.responseText);
            };
            xhr.onerror = function() {
                onComplete(null);
            };
            xhr.send();
        } else {
            onComplete(null);
        }
        return deferred.promise;
    }

    /**
     * Creates a new document.
     *
     * @param title
     * @returns {angular.$q.promise}
     */
    this.createDocument = function (title) {
      var deferred = $q.defer();
      var onComplete = function (result) {
        if (result && !result.error) {
          deferred.resolve(result);
        } else {
          deferred.reject(result);
        }
        $rootScope.$digest();
      };
      gapi.client.request({
        'path': '/drive/v2/files',
        'method': 'GET',
        'body': JSON.stringify({
          title: title,
          mimeType: 'application/vnd.google-apps.drive-sdk'
        })
      }).execute(onComplete);
      return deferred.promise;
    };

    /**
     * Checks to make sure the user is currently authorized and the access
     * token hasn't expired.
     *
     * @param immediateMode
     * @param userId
     * @returns {angular.$q.promise}
     */
    this.requireAuth = function (immediateMode, userId) {
      /* jshint camelCase: false */
      var token = gapi.auth.getToken();
      var now = Date.now() / 1000;

      console.log('---token---', token);

      if (token && ((token.expires_at - now) > (60))) {
        return $q.when(token);
      } else {
        var params = {
          'client_id': config.clientId,
          'scope': config.scopes,
          'immediate': immediateMode,
          'user_id': userId
        };
        var deferred = $q.defer();
        console.log(params);
        gapi.auth.authorize(params, function (result) {
          console.log('result', result);
          if (result && !result.error) {
            deferred.resolve(result);
          } else {
            deferred.reject(result);
          }
          $rootScope.$digest();
        });
        return deferred.promise;
      }
    };

    /**
     * Actually load a document. If the document is new, initializes
     * the model with an empty list of todos.
     *
     * @param id
     * @returns {angular.$q.promise}
     */
    this.load = function (id) {
      // var deferred = $q.defer();
      // var initialize = function (model) {
      //   model.getRoot().set('todos', model.createList());
      // };
      // var onLoad = function (document) {
      //   this.setDocument(id, document);
      //   deferred.resolve(document);
      //   $rootScope.$digest();
      // }.bind(this);
      // var onError = function (error) {
      //   if (error.type === gapi.drive.realtime.ErrorType.TOKEN_REFRESH_REQUIRED) {
      //     $rootScope.$emit('todos.token_refresh_required');
      //   } else if (error.type === gapi.drive.realtime.ErrorType.CLIENT_ERROR) {
      //     $rootScope.$emit('todos.client_error');
      //   } else if (error.type === gapi.drive.realtime.ErrorType.NOT_FOUND) {
      //     deferred.reject(error);
      //     $rootScope.$emit('todos.not_found', id);
      //   }
      //   $rootScope.$digest();
      // };
      // 
      gapi.client.load('drive', 'v2', function() {
        gapi.client.drive.files.get({
          'fileId' : id
        }).execute();
      });
      // gapi.drive.realtime.load(id, onLoad, initialize, onError);
      // return deferred.promise;
    };

    /**
     * Watches the model for any remote changes to force a digest cycle
     *
     * @param event
     */
    this.changeListener = function (event) {
      if (!event.isLocal) {
        $rootScope.$digest();
      }
    };

    this.setDocument = function (id, document) {
      document.getModel().getRoot().addEventListener(
        gapi.drive.realtime.EventType.OBJECT_CHANGED,
        this.changeListener);
      this.document = document;
      this.id = id;
    };

    this.updateFile = function(fileId, fileMetadata, fileData) {
        var boundary = '-------314159265358979323846';
        var delimiter = "\r\n--" + boundary + "\r\n";
        var close_delim = "\r\n--" + boundary + "--";

        var reader = new FileReader();
        var deferred = $q.defer();
        var onComplete = function (result) {
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
        };

        reader.readAsBinaryString(fileData);
        reader.onload = function(e) {
            var contentType = fileData.type || 'application/octet-stream';
            // Updating the metadata is optional and you can instead use the value from drive.files.get.
            var base64Data = btoa(reader.result);
            var multipartRequestBody =
                delimiter +
                'Content-Type: application/json\r\n\r\n' +
                JSON.stringify(fileMetadata) +
                delimiter +
                'Content-Type: ' + contentType + '\r\n' +
                'Content-Transfer-Encoding: base64\r\n' +
                '\r\n' +
                base64Data +
                close_delim;

            var request = gapi.client.request({
                'path': '/upload/drive/v2/files/' + fileId,
                'method': 'PUT',
                'params': {'uploadType': 'multipart', 'alt': 'json'},
                'headers': {
                  'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
                },
                'body': multipartRequestBody});
            // if (!callback) {
            //     callback = function(file) {
            //         console.log(file)
            //     };
            // }
            request.execute(onComplete);
        }
        return deferred.promise;
    }
  }]
);