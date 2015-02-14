'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:GeneCtrl
 * @description
 * # GeneCtrl
 * Controller of the oncokb
 */
angular.module('oncokb')
    .controller('GenesCtrl', ['$scope', '$location', '$routeParams', 'storage', 'documents', 'users', 'DTColumnDefBuilder', 'DTOptionsBuilder',
        function ($scope, $location, $routeParams, storage, Documents, users, DTColumnDefBuilder, DTOptionsBuilder) {
            $scope.getDocs = function() {
              var docs = Documents.get();
              if(docs.length > 0) {
                $scope.documents = Documents.get();
              }else{
                storage.requireAuth(true).then(function(){
                    storage.retrieveAllFiles().then(function(result){
                        Documents.set(result);
                        $scope.documents = Documents.get();
                    });
                });
              }
            };

            $scope.redirect = function(path) {
              $location.path(path);
            };

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
            $scope.getDocs();

            // var newGenes = ['RET','FGFR4','KIT','SMO','ALK1','ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K','MET','NOTCH','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2','RAD21','TET1','TET2','GATA1','GATA2','MPL','B2M','BTK','HLA-A','PRDM1','SF3B1','EPHA7','HIST1H1C','REL','CREBBP','VHL','PBRM1','SETD2','AKT2','AKT3','ERBB3','FGFR1','BRIP1','ERCC4','FANCA','FANCC','EWSR1','MDM2','MDM4','ETV6','DNMT1','EZH2','WT1','EP300','MYD88','CARD11','CD79B','KEAP1','MYC','FH','SDHA','SDHAF2','SDHB','SDHC','SDHD','RASA1','FOXA1','AURKA','AURKB','CCND1','CCNE1','PPP2R1A','CCND2','CCND3','PAK1','ERG','ETV1','SPOP','SOX2','MYOD1','CTCF','MTOR','PDGFRA','DAXX','MLH1','MSH6','RAD54L','RECQL4','BCL2','RB1','EIF1AX','EIF4A2','EIF4E','EPCAM','FAT1','SMAD2','SMAD3','SMAD4','TGFBR1','TGFBR2','U2AF1','BMPR1A','XPO1','ATRX','SMARCB1','CD276','CD274','CTLA4','TNFRSF14','IL7R','JAK1','JAK2','JAK3','IKZF1','ACVR1','IGF1','IGF1R','IGF2','INHA','INHBA','BAP1','NF2','RAF1','CDKN1B','RHOA','RAC1','MEN1','CDH1','STAG2','MDC1','MRE11A','POLD1','SOX9','ZFHX3','NF1','RAD50','RAD51B','RAD51C','RAD51D','RAD52','TOP1','HIST1H3A','KDM5C','KDM6A','KMT2A','KMT2C','KMT2D','H3F3A','H3F3B','H3F3C','KDM5A','POLE','DNMT3A','DNMT3B','AR','E2F3','FOXP1','RYBP','SHQ1','PAX8','TCEB1','CDKN2A','SMARCA4','TERT','BRCA1','BRCA2','PALB2','RHEB','TSC1','TSC2','RICTOR','ARID1A','ERCC2','CIC','FUBP1','HNF1A','MED12','BCOR','YAP1','LATS1','LATS2','MST1','ESR1','ATM','CHEK1','CHEK2','ATR','CENPA','MITF','FLT3','CEBPA','NPM1','RBM10','APC','ARAF','ARID1B','ARID2','ARID5B','BCL2L11','BRD4','CASP8','CBL','CDK12','CDK4','CDK6','CDKN1A','CDKN2B','GATA3','KDR','MAP2K2','MAP2K4','MAP3K1','MAX','MSH2','MYCN','NFE2L2','NFKBIA','NTRK1','NTRK2','NTRK3','PIK3CB','PIK3R2','PMS2','PTPN11','PTPRD','ROS1','RUNX1','SRC','TMPRSS2','XRCC2'];
            var newGenes = ['ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K','MET','NOTCH','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2'];
            
            $scope.create = function() {
              createDoc(0);
            };

            function createDoc(index) {
              if(index < newGenes.length) {
                storage.requireAuth().then(function () {
                  storage.createDocument(newGenes[index]).then(function (file) {
                    createDoc(++index);
                  });
                });
              }else {
                console.log('finished');
              }
            }
            
        }]
    )
    .controller('GeneCtrl', ['$scope', '$location', '$routeParams', 'storage', 'realtimeDocument', 'user', 'documents', 'OncoKB', 'gapi',
        function ($scope, $location, $routeParams, storage, realtimeDocument, User, Documents, OncoKB, gapi) {
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
                if (event.stopPropagation) { event.stopPropagation();}
                if (event.preventDefault) { event.preventDefault();}
                object.remove(index);
            };

            $scope.redo = function() {
              $scope.model.redo();
            };

            $scope.undo = function() {
              $scope.model.undo();
            };

            $scope.curatorsName = function() {
              return this.gene.curators.asArray().map(function(d){return d.name;}).join(', ');
            };

            $scope.curatorsEmail = function() {
              return this.gene.curators.asArray().map(function(d){return d.email;}).join(', ');
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
              $scope.fileEditable = file.editable?true:false;
              displayAllCollaborators($scope.realtimeDocument, bindDocEvents);
            }

            function valueChanged() {
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
                    var __curator = realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                    $scope.gene.curators.push(__curator);
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
        }]
    );
