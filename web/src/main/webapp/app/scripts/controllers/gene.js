'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:GeneCtrl
 * @description
 * # GeneCtrl
 * Controller of the oncokb
 */
angular.module('oncokb')
    .controller('GenesCtrl', ['$scope', '$location', '$routeParams', 'storage', 'documents', 'DTColumnDefBuilder', 'DTOptionsBuilder',
        function ($scope, $location, $routeParams, storage, Documents, DTColumnDefBuilder, DTOptionsBuilder) {
            $scope.documents = [];

            $scope.dtOptions = DTOptionsBuilder
              .newOptions()
              .withDOM('ifrtlp')
              .withBootstrap();
            $scope.dtColumns =  [
              DTColumnDefBuilder.newColumnDef(0),
              DTColumnDefBuilder.newColumnDef(1),
              DTColumnDefBuilder.newColumnDef(2).notSortable(),
              DTColumnDefBuilder.newColumnDef(3).notSortable(),
              DTColumnDefBuilder.newColumnDef(4).notSortable()
            ];

            $scope.createDoc = function() {
                if($scope.newDocName) {
                    storage.requireAuth().then(function () {
                        storage.createDocument($scope.newDocName.toString()).then(function (file) {
                            $location.url('/gene/' + file.id + '/');
                        });
                    }, function () {
                        $location.url('/gene');
                    });
                }
            };

            $scope.getDocs = function() {
              var docs = Documents.get();
              if(docs.length > 0) {
                $scope.documents = Documents.get();
              }else{
                storage.requireAuth(true).then(function(){
                    storage.retrieveAllFiles().then(function(result){
                        Documents.set(result).then(function(result){
                          $scope.documents = Documents.get();
                        });
                    });
                });
              }
            };

            $scope.redirect = function(path) {
              $location.path(path);
            };

            $scope.curateDoc = function() {
                $location.url('/gene/' + $scope.selectedDoc.id + '/');
            };

            $scope.getDocs();

            function getDocumentFromList(index, documents) {
                if($scope._documents && $scope._documents.length > index) {
                    storage.getDocument($scope._documents[index].id).then(function(file){
                        console.log(file);
                        if(file.editable) {
                            documents.push(file);
                        }
                        getDocumentFromList(++index, documents);
                    });
                }else {
                    $scope.documents = documents;
                }
            }
        }]
    )
    .controller('GeneCtrl', ['$scope', '$location', '$routeParams', 'storage', 'realtimeDocument', 'user', 'documents',
        function ($scope, $location, $routeParams, storage, realtimeDocument, User, Documents) {
            $scope.fileTitle = $routeParams.geneName;
            $scope.realtimeDocument = realtimeDocument;
            $scope.gene = '';
            $scope.newGene = {};
            $scope.newMutation = {};
            $scope.newTumorType = {};
            $scope.newTI = [{},{},{},{}];
            $scope.newTrial = '';
            $scope.collaborators = {};
            $scope.checkboxes = {
                'oncogenic': ['YES', 'NO', 'Unknown']
            };
            $scope.fileEditable = false;

            if($scope.fileTitle) {
                var model = realtimeDocument.getModel();
                if(!model.getRoot().get('gene')) {
                  var gene = model.create('Gene');
                  model.getRoot().set('gene', gene);
                  $scope.gene = gene;
                  $scope.gene.name.setText($scope.fileTitle);
                  $scope.model =  model;
                  afterCreateGeneModel();
                }else {
                  $scope.gene = model.getRoot().get('gene');
                  $scope.model =  model;
                  afterCreateGeneModel();
                }
            }else {
              $scope.model = '';
            }

            $scope.authorize = function(){
                print($routeParams);
                    storage.requireAuth(false).then(function () {
                    var target = $location.search().target;
                    if (target) {
                        $location.url(target);
                    } else {
                        storage.getDocument('1rFgBCL0ftynBxRl5E6mgNWn0WoBPfLGm8dgvNBaHw38').then(function(file){
                            storage.downloadFile(file).then(function(text) {
                                $scope.curateFile = text;
                            });
                        });
                    }
                });
            };

            $scope.addGene = function() {
                if (this.newGene && this.newGene.name) {
                    realtimeDocument.getModel().beginCompoundOperation();
                    var gene = realtimeDocument.getModel().create(Oncokb.Gene, this.newGene);
                    this.newGene = {};
                    this.gene = gene;
                    realtimeDocument.getModel().endCompoundOperation();
                }
            };

            $scope.addMutation = function() {
                if (this.gene && this.newMutation && this.newMutation.name) {
                    var _mutation = '';
                    realtimeDocument.getModel().beginCompoundOperation();
                    _mutation = realtimeDocument.getModel().create(OncoKB.Mutation);
                    _mutation.name.setText(this.newMutation.name);
                    this.gene.mutations.push(_mutation);
                    realtimeDocument.getModel().endCompoundOperation();
                    this.newMutation = {};
                }
            };

            $scope.addTumorType = function(mutation) {
                if (mutation && this.newTumorType && this.newTumorType.name) {
                    var _tumorType = '';
                    realtimeDocument.getModel().beginCompoundOperation();
                    _tumorType = realtimeDocument.getModel().create(OncoKB.Tumor);
                    _tumorType.name.setText(this.newTumorType.name);
                    for(var i=0; i<4; i++) {
                      var __ti = realtimeDocument.getModel().create(OncoKB.TI);
                      var __status = i<2?1:0; // 1: Standard, 0: Investigational
                      var __type = i%2===0?1:0; //1: sensitivity, 0: resistance
                      var __name = (__status?'Standard':'Investigational') + ' therapeutic implications for drug ' + (__type?'sensitivity':'resistance');
                      
                      __ti.types.set('status', __status.toString());
                      __ti.types.set('type', __type.toString());
                      __ti.name.setText(__name);
                      _tumorType.TI.push(__ti);
                    }
                    mutation.tumors.push(_tumorType);
                    realtimeDocument.getModel().endCompoundOperation();
                    this.newTumorType = {};
                }
            };

            //Add new therapeutic implication
            $scope.addTI = function(ti, index) {
                if (ti && this.newTI[index] && this.newTI[index].name) {
                    var _treatment = '';
                    realtimeDocument.getModel().beginCompoundOperation();
                    _treatment = realtimeDocument.getModel().create(OncoKB.Treatment);
                    _treatment.name.setText(this.newTI[index].name);
                    _treatment.type.setText('Therapy');
                    ti.treatments.push(_treatment);
                    console.log(_treatment);
                    realtimeDocument.getModel().endCompoundOperation();
                    this.newTI[index] = {};
                }
            };

            //Add new therapeutic implication
            $scope.addTrial = function(trials) {
                if (trials && this.newTrial) {
                    trials.push(this.newTrial);
                    this.newTrial = '';
                }
            };

            $scope.checkScope = function() {
                print($scope.gene);
                print($scope.gene.mutations.asArray());
                print($scope.gene.mutations.asArray()[1].tumors.asArray());
                print($scope.collaborators);
            };

            $scope.remove = function(index, object, event) {
                if (event.stopPropagation) event.stopPropagation();
                if (event.preventDefault) event.preventDefault();
                object.remove(index);
            };

            $scope.redo = function() {
              $scope.model.redo();
            };

            $scope.undo = function() {
              $scope.model.undo();
            };

            $scope.curatorsName = function() {
              return this.gene.curators.asArray().map(function(d){return d.name}).join(', ');
            };

            $scope.curatorsEmail = function() {
              return this.gene.curators.asArray().map(function(d){return d.email}).join(', ');
            };

            $scope.removeCurator = function(index) {
              $scope.gene.curators.remove(index);
            };

            function bindDocEvents() {
              $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_JOINED, displayCollaboratorEvent);
              $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_LEFT, displayCollaboratorEvent);
              $scope.model.addEventListener(gapi.drive.realtime.EventType.UNDO_REDO_STATE_CHANGED, onUndoStateChanged);
              $scope.gene.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChanged);
            }

            function afterCreateGeneModel() {
              var file = Documents.get({title: $scope.fileTitle});
              file = file[0];
              console.log('file', file);
              console.log('or editable', file.editable);
              $scope.fileEditable = file.editable?true:false;
              console.log('file editable', $scope.fileEditable);
              displayAllCollaborators($scope.realtimeDocument, bindDocEvents);
            }

            function valueChanged(evt) {
              if($scope.gene) {
                var hasCurator = false;
                if($scope.gene.curators && angular.isArray($scope.gene.curators.asArray()) && $scope.gene.curators.asArray().length > 0) {
                  var _array = $scope.gene.curators.asArray();
                  for(var i=0; i<_array.length; i++) {
                    if(_array[i].email.text === User.email) {
                      hasCurator = true;
                      break;
                    }
                  }

                  if(!hasCurator) {
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    var _curator = realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                    $scope.gene.curators.push(_curator);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                  }
                }else {
                  $scope.realtimeDocument.getModel().beginCompoundOperation();
                  var _curator = realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                  $scope.gene.curators.push(_curator);
                  $scope.realtimeDocument.getModel().endCompoundOperation();
                }
              }
            }

            function displayCollaboratorEvent(evt) {
              print(evt);
              switch (evt.type) {
                case 'collaborator_left':
                  removeCollaborator(evt.collaborator);
                  break;
                case 'collaborator_joined':
                  addCollaborator(evt.collaborator);
                  break;
                default:
                  console.info('Unknown event:', evt);
                  break;
              }
              $scope.$apply($scope.collaborators);
            }

            function addCollaborator(user) {
              if(!$scope.collaborators.hasOwnProperty(user.userId)) {
                $scope.collaborators[user.sessionId] = {};
              }
              $scope.collaborators[user.sessionId] = user;
              print(user);
            }

            function removeCollaborator(user) {
              if(!$scope.collaborators.hasOwnProperty(user.sessionId)) {
                console.log('Unknown collaborator:', user);
              }else {
                delete $scope.collaborators[user.sessionId];
              }
            }

            function displayAllCollaborators(document, callback) {
              var collaborators = document.getCollaborators();
              var collaboratorCount = collaborators.length;
              var _user = {};
              for (var i = 0; i < collaboratorCount; i++) {
                var user = collaborators[i];
                if(!$scope.collaborators.hasOwnProperty(user.userId)) {
                  $scope.collaborators[user.sessionId] = {};
                }
                $scope.collaborators[user.sessionId] = user;
                if(user.isMe) {
                  _user = user;
                }
              }

              if(User.email === 'N/A') {
                storage.getUserInfo(_user.userId).then(function(userInfo){
                  User.name = userInfo.displayName;
                  User.email = angular.isArray(userInfo.emails)?(userInfo.emails.length>0?userInfo.emails[0].value:'N/A'):userInfo.emails;
                  callback();
                });
              }else {
                callback();
              }
            }

            function onUndoStateChanged(evt) {
              console.info(evt);

              if (evt.canUndo) {
                $scope.canUndo = true;
              }else {
                $scope.canUndo = false;
              }
              if (evt.canRedo) {
                $scope.canRedo = true;
              }else {
                $scope.canRedo = false;
              }
            }

            function print(item) {
              console.log('\n---------------------------------');
              console.log(item);
              console.log('---------------------------------\n');
            }
        }]
    );
