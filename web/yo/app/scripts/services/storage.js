'use strict';

angular.module('oncokbApp')
    .service('storage', ['$q', '$rootScope', '$route', 'config', 'gapi', 'stringUtils', 'dialogs',
        function($q, $rootScope, $route, config, gapi, stringUtils, dialogs) {
            var self = {};
            self.id = null;
            self.document = null;
            self.metaDoc = null;
            /**
             * Close the current document.
             */
            self.closeDocument = function() {
                if (self.document) {
                    self.document.close();
                }
                self.document = null;
                self.id = null;
            };

            /**
             * Ensure the document is loaded.
             *
             * @param {string} id realtime document id
             * @return {angular.$q.promise} Promise
             */
            self.getDocument = function(id) {
                var deferred = $q.defer();
                var onComplete = function(result) {
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
                        fileId: id
                    }).execute(onComplete);
                });
                return deferred.promise;
            };

            self.getPermission = function(id) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                };

                gapi.client.drive.permissions.list({
                    fileId: id
                }).execute(onComplete);

                return deferred.promise;
            };

            self.createFolder = function(parentID) {
                var deferred = $q.defer();
                var onComplete = function(result) {
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
                    title: date.toString(),
                    parents: [{id: parentID}],
                    mimeType: 'application/vnd.google-apps.folder'
                };
                var request = gapi.client.drive.files.insert({
                    resource: body
                });
                request.execute(onComplete);

                return deferred.promise;
            };

            self.insertPermission = function(id, email, type, role) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    // console.log(result);
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                };

                var body = {
                    value: email,
                    type: type,
                    role: role
                };
                var request = gapi.client.drive.permissions.insert({
                    fileId: id,
                    sendNotificationEmails: false,
                    resource: body
                });
                request.execute(onComplete);

                return deferred.promise;
            };

            self.updatePermission = function(fileId, permissionId, newRole) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    // console.log(result);
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                };

                var request = gapi.client.drive.permissions.get({
                    fileId: fileId,
                    permissionId: permissionId
                });
                request.execute(function(resp) {
                    resp.role = newRole;
                    var updateRequest = gapi.client.drive.permissions.update({
                        fileId: fileId,
                        permissionId: permissionId,
                        resource: resp
                    });
                    updateRequest.execute(onComplete);
                });

                return deferred.promise;
            };

            self.deletePermission = function(fileId, permissionId) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    // console.log(result);
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                };

                var request = gapi.client.drive.permissions.delete({
                    fileId: fileId,
                    permissionId: permissionId
                });
                request.execute(onComplete);

                return deferred.promise;
            };

            self.getUserInfo = function(userId) {
                var deferred = $q.defer();
                var onComplete = function(result) {
                    // console.log('get user info', result);
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                };
                gapi.client.load('plus', 'v1', function() {
                    gapi.client.plus.people.get({
                        userId: userId
                    }).execute(onComplete);
                });
                return deferred.promise;
            };

            /**
             * Ensure the document is loaded.
             *
             * @param {string} id realtime document id
             * @return {angular.$q.promise} Promise
             */
            self.getRealtimeDocument = function(id) {
                if (self.id === id) {
                    return $q.when(self.document);
                } else if (self.document) {
                    self.closeDocument();
                }
                return self.load(id);
            };
            /**
             * Retrive meta file
             * @param {string} id realtime document id
             * */
            self.getMetaRealtimeDocument = function(id) {
                var deferred = $q.defer();
                var initialize = function() {
                };
                var onLoad = function(document) {
                    self.metaDoc = document;
                    deferred.resolve(document);
                    $rootScope.$digest();
                };
                var onError = function(error) {
                    console.log('error', error);
                };
                gapi.drive.realtime.load(id, onLoad, initialize, onError);
                return deferred.promise;

            };
            /**
             * Retrieve a list of File resources.
             * @return {d.promise|promise|*|h.promise|f} Promise
             */
            self.retrieveAllFiles = function() {
                var deferred = $q.defer();

                var retrievePageOfFiles = function(request, result) {
                    request.execute(function(resp) {
                        result = result.concat(resp.items);
                        var nextPageToken = resp.nextPageToken;
                        if (nextPageToken) {
                            request = gapi.client.drive.files.list({
                                q: '"' + config.folderId + '" in parents',
                                pageToken: nextPageToken,
                                maxResults: 300
                            });
                            retrievePageOfFiles(request, result);
                        } else {
                            // console.log('get all files', result);
                            deferred.resolve(result);
                        }
                    });
                };

                gapi.client.load('drive', 'v2', function() {
                    var initialRequest = gapi.client.drive.files.list({
                        q: '"' + config.folderId + '" in parents'
                    });
                    retrievePageOfFiles(initialRequest, []);
                });
                return deferred.promise;
            };
            /**
             *
             * */
            self.retrieveMeta = function() {
                var deferred = $q.defer();

                var retrievePageOfFiles = function(request, result) {
                    request.execute(function(resp) {
                        result = result.concat(resp.items);
                        var nextPageToken = resp.nextPageToken;
                        if (nextPageToken) {
                            request = gapi.client.drive.files.list({
                                q: '"' + config.metaFolderId + '" in parents',
                                pageToken: nextPageToken
                            });
                            retrievePageOfFiles(request, result);
                        } else {
                            // console.log('get all files', result);
                            deferred.resolve(result);
                        }
                    });
                };

                gapi.client.load('drive', 'v2', function() {
                    var initialRequest = gapi.client.drive.files.list({
                        q: '"' + config.metaFolderId + '" in parents'
                    });
                    retrievePageOfFiles(initialRequest, []);
                });
                return deferred.promise;
            };
            /**
             *  Retrieve a list of File resources.
             * @return {d.promise|promise|*|h.promise|f} Promise
             */
            self.retrieveAllChildrenFiles = function() {
                var deferred = $q.defer();
                var retrievePageOfFiles = function(request, result) {
                    request.execute(function(resp) {
                        result = result.concat(resp.items);
                        var nextPageToken = resp.nextPageToken;
                        if (nextPageToken) {
                            request = gapi.client.drive.children.list({
                                folderId: config.folderId,
                                pageToken: nextPageToken
                            });
                            retrievePageOfFiles(request, result);
                        } else {
                            deferred.resolve(result);
                        }
                    });
                };
                gapi.client.load('drive', 'v2', function() {
                    var initialRequest = gapi.client.drive.children.list({
                        folderId: config.folderId
                    });
                    retrievePageOfFiles(initialRequest, []);
                });
                return deferred.promise;
            };

            self.downloadFile = function(file) {
                var deferred = $q.defer();
                var onComplete = function(result) {
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
            };

            /**
             * Creates a new document.
             *
             * @param {string} title New document title
             * @param {string} parentId The parent folder Id which is used to store the new document
             * @return {angular.$q.promise} Promise
             */
            self.createDocument = function(title, parentId) {
                var deferred = $q.defer();
                var onComplete = function(result) {
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
                    path: '/drive/v2/files',
                    method: 'POST',
                    body: JSON.stringify({
                        title: title,
                        parents: [{
                            id: parentId || config.folderId
                        }],
                        mimeType: 'application/vnd.google-apps.drive-sdk'
                    })
                }).execute(onComplete);
                return deferred.promise;
            };

            self.checkToken = function() {
                var token = gapi.auth.getToken();
                var now = Date.now() / 1000;

                if (token && ((token.expires_at - now) > (60))) {
                    return true;
                }
                return false;
            };

            /**
             * Checks to make sure the user is currently authorized and the access
             * token hasn't expired.
             *
             * @param {boolean} immediateMode Immediate Mode
             * @param {string} userId User id
             * @return {angular.$q.promise} Promise
             */
            self.requireAuth = function(immediateMode, userId) {
                var token = gapi.auth.getToken();
                var now = Date.now() / 1000;

                if (token && ((token.expires_at - now) > (600))) {
                    // console.log('token unexpires');
                    return $q.when(token);
                }
                /* eslint camelcase: ["error", {properties: "never"}]*/
                var params = {
                    client_id: config.clientId,
                    scope: config.scopes,
                    immediate: immediateMode,
                    user_id: userId
                };
                var deferred = $q.defer();

                console.log('token expired, call authorize function');
                gapi.auth.authorize(params, function(result) {
                    // console.log('get authorize', result);
                    if (result && !result.error) {
                        deferred.resolve(result);
                    } else {
                        deferred.reject(result);
                    }
                    $rootScope.$digest();
                });
                return deferred.promise;
            };

            /**
             * Actually load a document. If the document is new, initializes
             * the model with an empty list of todos.
             *
             * @param {string} id realtime document id
             * @return {angular.$q.promise} Promise
             */
            self.load = function(id) {
                var deferred = $q.defer();
                var initialize = function() {
                };
                var onLoad = function(document) {
                    self.setDocument(id, document);
                    deferred.resolve(document);
                    $rootScope.$digest();
                };
                var onError = function(error) {
                    var errorMessage = error.toString();
                    var sendEmail = true;

                    if (error.type === gapi.drive.realtime.ErrorType.TOKEN_REFRESH_REQUIRED) {
                        sendEmail = false;
                        self.requireAuth(true).then(function(result) {
                            if (result && !result.error) {
                                console.log('\t Renewed token', new Date().getTime(), gapi.auth.getToken());
                            } else {
                                dialogs.error('Error', 'Your Google account token is expired. The page is going to be reloaded. ' +
                                    'If you continue seeing this issue. Please contact OncoKB team.');
                                $route.reload();
                            }
                        });
                    } else if (error.type === gapi.drive.realtime.ErrorType.CLIENT_ERROR) {
                        console.log('error: realtimeDoc.client_error');
                        $rootScope.$emit('realtimeDoc.client_error');
                    } else if (error.type === gapi.drive.realtime.ErrorType.NOT_FOUND) {
                        console.log('error: realtimeDoc.not_found');
                        deferred.reject(error);
                        $rootScope.$emit('realtimeDoc.not_found', id);
                    } else {
                        console.log(error, id);
                        $rootScope.$emit('realtimeDoc.other_error');
                    }
                    if (sendEmail) {
                        if (error.isFatal && self.document) {
                            var gene = self.document.getModel().getRoot().get('gene');
                            var vus = self.document.getModel().getRoot().get('vus');
                            var geneData = stringUtils.getGeneData(gene, false, false, false);
                            var vusData = stringUtils.getVUSFullData(vus, false);
                            errorMessage += '\n\ngene: ' + geneData +
                                '\n\nVUS: ' + vusData;
                        }
                        $rootScope.$emit('oncokbError', {
                            message: errorMessage,
                            reason: error.type + ', Is fatal? ' + error.isFatal
                        });
                    }
                    $rootScope.$digest();
                };
                gapi.drive.realtime.load(id, onLoad, initialize, onError);
                return deferred.promise;
            };

            /**
             * Watches the model for any remote changes to force a digest cycle
             *
             * @param {object} event Event
             */
            self.changeListener = function(event) {
                if (!event.isLocal) {
                    $rootScope.$digest();
                }
            };

            self.setDocument = function(id, document) {
                document.getModel().getRoot().addEventListener(
                    gapi.drive.realtime.EventType.OBJECT_CHANGED,
                    self.changeListener);
                self.document = document;
                self.id = id;
            };

            self.updateFile = function(fileId, fileMetadata, fileData) {
                var boundary = '-------314159265358979323846';
                var delimiter = '\r\n--' + boundary + '\r\n';
                var closeDelim = '\r\n--' + boundary + '--';

                var reader = new FileReader();
                var deferred = $q.defer();
                var onComplete = function(result) {
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
                        closeDelim;

                    var request = gapi.client.request({
                        path: '/upload/drive/v2/files/' + fileId,
                        method: 'PUT',
                        params: {uploadType: 'multipart', alt: 'json'},
                        headers: {
                            'Content-Type': 'multipart/mixed; boundary="' + boundary + '"'
                        },
                        body: multipartRequestBody
                    });

                    request.execute(onComplete);
                };
                return deferred.promise;
            };
            return self;
        }]
    );
