'use strict';

/**
 * @ngdoc service
 * @name oncokb.storage
 * @description
 * # storage
 * Service in the oncokb.
 */
angular.module('oncokbApp')
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
      if(this.document){
        this.document.close();
      }
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
          // console.log('get document', result);
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
    };

    this.getPermission = function (id) {
        var deferred = $q.defer();
        var onComplete = function (result) {
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
            $rootScope.$digest();
        };

        gapi.client.drive.permissions.list({
          'fileId' : id
        }).execute(onComplete);

        return deferred.promise;
    };

    this.createFolder = function(parentID) {
      var deferred = $q.defer();
      var onComplete = function (result) {
        // console.log(result);
          if (result && !result.error) {
            deferred.resolve(result);
          } else {
            deferred.reject(result);
          }
          $rootScope.$digest();
      };
      var date = new Date();
      var body = {
        'title': date.toString(),
        'parents': [{'id': parentID}],
        'mimeType': 'application/vnd.google-apps.folder'
      };
      var request = gapi.client.drive.files.insert({
        'resource': body
      });
      request.execute(onComplete);

      return deferred.promise;
    };

    this.insertPermission = function (id, email, type, role) {
        var deferred = $q.defer();
        var onComplete = function (result) {
          // console.log(result);
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
            $rootScope.$digest();
        };

        var body = {
          'value': email,
          'type': type,
          'role': role
        };
        var request = gapi.client.drive.permissions.insert({
          'fileId': id,
          'sendNotificationEmails': false,
          'resource': body
        });
        request.execute(onComplete);

        return deferred.promise;
    };

    this.updatePermission = function (fileId, permissionId, newRole) {
        var deferred = $q.defer();
        var onComplete = function (result) {
          // console.log(result);
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
            $rootScope.$digest();
        };

        var request = gapi.client.drive.permissions.get({
          'fileId': fileId,
          'permissionId': permissionId
        });
        request.execute(function(resp) {
          resp.role = newRole;
          var updateRequest = gapi.client.drive.permissions.update({
            'fileId': fileId,
            'permissionId': permissionId,
            'resource': resp
          });
          updateRequest.execute(onComplete);
        });

        return deferred.promise;
    };

    this.getUserInfo = function (userId) {
        var deferred = $q.defer();
        var onComplete = function (result) {
          // console.log('get user info', result);
            if (result && !result.error) {
              deferred.resolve(result);
            } else {
              deferred.reject(result);
            }
            $rootScope.$digest();
        };
        gapi.client.load('plus','v1', function(){
            gapi.client.plus.people.get({
                'userId' : userId
            }).execute(onComplete);
        });
        return deferred.promise;
    };

    /**
     * Ensure the document is loaded.
     *
     * @param id
     * @returns {angular.$q.promise}
     */
    this.getRealtimeDocument = function (id) {
        if (this.id === id) {
            return $q.when(this.document);
        } else if (this.document) {
            this.closeDocument();
        }
        return this.load(id);
    };

    /**
     * Retrieve a list of File resources.
     *
     * @param {Function} callback Function to call when the request is complete.
     */
    this.retrieveAllFiles = function() {
        var retrievePageOfFiles = function(request, result) {
            request.execute(function(resp) {
              result = result.concat(resp.items);
              var nextPageToken = resp.nextPageToken;
              if (nextPageToken) {
                request = gapi.client.drive.files.list({
                    'q' : '"' + config.folderId + '" in parents',
                    'pageToken': nextPageToken,
                    'maxResults': 300
                });
                retrievePageOfFiles(request, result);
              } else {
                // console.log('get all files', result);
                deferred.resolve(result);
              }
            });
        };

        var deferred = $q.defer();
        gapi.client.load('drive', 'v2', function() {
            var initialRequest = gapi.client.drive.files.list({
                    'q' : '"' + config.folderId + '" in parents'
                });
            retrievePageOfFiles(initialRequest, []);
        });
        return deferred.promise;
    };

    /**
     * Retrieve a list of File resources.
     *
     * @param {Function} callback Function to call when the request is complete.
     */
    this.retrieveAllChildrenFiles = function() {
        var retrievePageOfFiles = function(request, result) {
            request.execute(function(resp) {
              result = result.concat(resp.items);
              var nextPageToken = resp.nextPageToken;
              if (nextPageToken) {
                request = gapi.client.drive.children.list({
                    'folderId' : config.folderId,
                    'pageToken': nextPageToken
                });
                retrievePageOfFiles(request, result);
              } else {
                deferred.resolve(result);
              }
            });
        };
        
        var deferred = $q.defer();
        gapi.client.load('drive', 'v2', function() {
            var initialRequest = gapi.client.drive.children.list({
                    'folderId' : config.folderId
                });
            retrievePageOfFiles(initialRequest, []);
        });
        return deferred.promise;
    };

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
          /* jshint -W106 */
          var accessToken = gapi.auth.getToken().access_token;
          /* jshint +W106 */
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
    };

    /**
     * Creates a new document.
     *
     * @param title
     * @returns {angular.$q.promise}
     */
    this.createDocument = function (title, parentId) {
      var deferred = $q.defer();
      var onComplete = function (result) {
      // console.log('Completes', result);
        if (result && !result.error) {
          deferred.resolve(result);
        } else {
          deferred.reject(result);
        }
        $rootScope.$digest();
      };
      // console.log('Start create file');
      gapi.client.request({
        'path': '/drive/v2/files',
        'method': 'POST',
        'body': JSON.stringify({
          'title': title,
          'parents': [{
            'id': parentId || config.folderId
          }],
          'mimeType': 'application/vnd.google-apps.drive-sdk'
        })
      }).execute(onComplete);
      return deferred.promise;
    };

    this.checkToken = function() {
        var token = gapi.auth.getToken();
        var now = Date.now() / 1000;

        if (token && ((token.expires_at - now) > (60))) {
            return true;
        } else {
            return false;
        }
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
      var token = gapi.auth.getToken();
      var now = Date.now() / 1000;

      if (token && ((token.expires_at - now) > (600))) {
        console.log('token unexpires');
        return $q.when(token);
      } else {
        var params = {
          'client_id': config.clientId,
          'scope': config.scopes,
          'immediate': immediateMode,
          'user_id': userId
        };
        var deferred = $q.defer();
        
        console.log('token expired, call authorize function');
        gapi.auth.authorize(params, function (result) {
          // console.log('get authorize', result);
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
      var deferred = $q.defer();
      var initialize = function () {};
      var onLoad = function (document) {
        this.setDocument(id, document);
        deferred.resolve(document);
        $rootScope.$digest();
      }.bind(this);
      var onError = function (error) {
        console.log('load on error', error);
        if (error.type === gapi.drive.realtime.ErrorType.TOKEN_REFRESH_REQUIRED) {
          console.log('error: realtimeDoc.token_refresh_required');
          $rootScope.$emit('realtimeDoc.token_refresh_required');
        } else if (error.type === gapi.drive.realtime.ErrorType.CLIENT_ERROR) {
          console.log('error: realtimeDoc.client_error');
          $rootScope.$emit('realtimeDoc.client_error');
        } else if (error.type === gapi.drive.realtime.ErrorType.NOT_FOUND) {
          console.log('error: realtimeDoc.not_found');
          deferred.reject(error);
          $rootScope.$emit('realtimeDoc.not_found', id);
        }else {
          console.log(error, id);
          $rootScope.$emit('realtimeDoc.other_error');
        }
        $rootScope.$digest();
      };
      gapi.drive.realtime.load(id, onLoad, initialize, onError);
      return deferred.promise;
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
        var delimiter = '\r\n--' + boundary + '\r\n';
        var close_delim = '\r\n--' + boundary + '--';

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
        reader.onload = function() {
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

            request.execute(onComplete);
        };
        return deferred.promise;
    };
  }]
);