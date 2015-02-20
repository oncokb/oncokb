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
                // $scope.$apply(function() {
                  $scope.documents = Documents.get();
                  $scope.loaded = true;
                // });
              }else{
                storage.requireAuth(true).then(function(){
                    storage.retrieveAllFiles().then(function(result){
                        Documents.set(result);
                        $scope.documents = Documents.get();
                        $scope.loaded = true;
                        // loading_screen.finish();
                    });
                });
              }
            };

            $scope.redirect = function(path) {
              $location.path(path);
            };

            $scope.dtOptions = DTOptionsBuilder
              .newOptions()
              .withDOM('ifrtlp')
              .withBootstrap();

            $scope.dtColumns =  [
              DTColumnDefBuilder.newColumnDef(0),
              DTColumnDefBuilder.newColumnDef(1).notSortable(),
              DTColumnDefBuilder.newColumnDef(2).notSortable(),
              DTColumnDefBuilder.newColumnDef(3).notSortable()
            ];
            $scope.loaded = false;
            $scope.getDocs();

            $scope.$watch('loaded', function(newValue, oldValue, scope) {
              
            });
            // var newGenes = ['RET','FGFR4','KIT','SMO','ALK1','ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K1','MET','NOTCH1','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2','RAD21','TET1','TET2','GATA1','GATA2','MPL','B2M','BTK','HLA-A','PRDM1','SF3B1','EPHA7','HIST1H1C','REL','CREBBP','VHL','PBRM1','SETD2','AKT2','AKT3','ERBB3','FGFR1','BRIP1','ERCC4','FANCA','FANCC','EWSR1','MDM2','MDM4','ETV6','DNMT1','EZH2','WT1','EP300','MYD88','CARD11','CD79B','KEAP1','MYC','FH','SDHA','SDHAF2','SDHB','SDHC','SDHD','RASA1','FOXA1','AURKA','AURKB','CCND1','CCNE1','PPP2R1A','CCND2','CCND3','PAK1','ERG','ETV1','SPOP','SOX2','MYOD1','CTCF','MTOR','PDGFRA','DAXX','MLH1','MSH6','RAD54L','RECQL4','BCL2','RB1','EIF1AX','EIF4A2','EIF4E','EPCAM','FAT1','SMAD2','SMAD3','SMAD4','TGFBR1','TGFBR2','U2AF1','BMPR1A','XPO1','ATRX','SMARCB1','CD276','CD274','CTLA4','TNFRSF14','IL7R','JAK1','JAK2','JAK3','IKZF1','ACVR1','IGF1','IGF1R','IGF2','INHA','INHBA','BAP1','NF2','RAF1','CDKN1B','RHOA','RAC1','MEN1','CDH1','STAG2','MDC1','MRE11A','POLD1','SOX9','ZFHX3','NF1','RAD50','RAD51B','RAD51C','RAD51D','RAD52','TOP1','HIST1H3A','KDM5C','KDM6A','KMT2A','KMT2C','KMT2D','H3F3A','H3F3B','H3F3C','KDM5A','POLE','DNMT3A','DNMT3B','AR','E2F3','FOXP1','RYBP','SHQ1','PAX8','TCEB1','CDKN2A','SMARCA4','TERT','BRCA1','BRCA2','PALB2','RHEB','TSC1','TSC2','RICTOR','ARID1A','ERCC2','CIC','FUBP1','HNF1A','MED12','BCOR','YAP1','LATS1','LATS2','MST1','ESR1','ATM','CHEK1','CHEK2','ATR','CENPA','MITF','FLT3','CEBPA','NPM1','RBM10','APC','ARAF','ARID1B','ARID2','ARID5B','BCL2L11','BRD4','CASP8','CBL','CDK12','CDK4','CDK6','CDKN1A','CDKN2B','GATA3','KDR','MAP2K2','MAP2K4','MAP3K1','MAX','MSH2','MYCN','NFE2L2','NFKBIA','NTRK1','NTRK2','NTRK3','PIK3CB','PIK3R2','PMS2','PTPN11','PTPRD','ROS1','RUNX1','SRC','TMPRSS2','XRCC2'];
            var newGenes = ['ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K1','MET','NOTCH1','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2'];
            
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
    .controller('GeneCtrl', ['_','$resource', '$scope', '$location', '$route', '$routeParams', 'storage', 'loadFile', 'user', 'documents', 'OncoKB', 'gapi', 'DatabaseConnector',
        function (_, $resource, $scope, $location, $route, $routeParams, storage, loadFile, User, Documents, OncoKB, gapi, DatabaseConnector) {
            $scope.authorize = function(){
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
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    _mutation = $scope.realtimeDocument.getModel().create(OncoKB.Mutation);
                    _mutation.name.setText(this.newMutation.name);
                    this.gene.mutations.push(_mutation);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                    this.newMutation = {};
                }
            };

            $scope.addTumorType = function(mutation) {
                if (mutation && this.newTumorType && this.newTumorType.name) {
                    var _tumorType = '';
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    _tumorType = $scope.realtimeDocument.getModel().create(OncoKB.Tumor);
                    _tumorType.name.setText(this.newTumorType.name);
                    _tumorType.nccn.category.setText('2A');
                    for(var i=0; i<4; i++) {
                      var __ti = $scope.realtimeDocument.getModel().create(OncoKB.TI);
                      var __status = i<2?1:0; // 1: Standard, 0: Investigational
                      var __type = i%2===0?1:0; //1: sensitivity, 0: resistance
                      var __name = (__status?'Standard':'Investigational') + ' implications for ' + (__type?'sensitivity':'resistance') + ' to therapy';
                      
                      __ti.types.set('status', __status.toString());
                      __ti.types.set('type', __type.toString());
                      __ti.name.setText(__name);
                      _tumorType.TI.push(__ti);
                    }
                    mutation.tumors.push(_tumorType);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                    this.newTumorType = {};
                }
            };

            //Add new therapeutic implication
            $scope.addTI = function(ti, index) {
                if (ti && this.newTI[index] && this.newTI[index].name) {
                    var _treatment = '';
                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    _treatment = $scope.realtimeDocument.getModel().create(OncoKB.Treatment);
                    _treatment.name.setText(this.newTI[index].name);
                    _treatment.type.setText('Therapy');
                    ti.treatments.push(_treatment);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
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
                // print($scope.gene);
                // print($scope.gene.mutations.asArray());
                // print($scope.gene.mutations.asArray()[1].tumors.asArray());
                // print($scope.collaborators);
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

            $scope.checkTI = function(TI, status, type) {
              var _status = TI.types.get('status').toString();
              var _type = TI.types.get('type').toString();
              status = status.toString();
              type = type.toString();
              if(_status === status && _type === type) {
                return true;
              }else {
                return false;
              }
            };

            $scope.mutationEffectChanged = function(mutationEffect) {
              mutationEffect.addOn.setText('');
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
                    var __curator = $scope.realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                    $scope.gene.curators.push(__curator);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                  }
                }else {
                  $scope.realtimeDocument.getModel().beginCompoundOperation();
                  var _curator = $scope.realtimeDocument.getModel().create(OncoKB.Curator, User.name, User.email);
                  $scope.gene.curators.push(_curator);
                  $scope.realtimeDocument.getModel().endCompoundOperation();
                }
              }
            }

            function displayCollaboratorEvent(evt) {
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

            function getLevels() {
              var des = {
                '': '',
                '1': 'FDA-approved biomarker and drug association in this indication.',
                '2A': 'FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed for this indication.',
                '2B': 'FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed for this indication.',
                '3': 'Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association.',
                '4': 'Preclinical evidence potentially links this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association.'
              };

              var levels = {};

              var levelsCategories = {
                SS: ['','1','2A'],
                SR: ['','2A'],
                IS: ['','2B','3','4'],
                IR: ['','2B','3','4']
              }

              for(var key in levelsCategories) {
                var _items = levelsCategories[key];
                levels[key] = [];
                for (var i = 0; i < _items.length; i++) {
                  var __datum = {};
                  __datum.label = _items[i] + (_items[i]===''?'':' - ') + des[_items[i]];
                  __datum.value = _items[i];
                  levels[key].push(__datum);
                }
              }
              return levels;
            }

            $scope.fileTitle = $routeParams.geneName;
            $scope.gene = '';
            $scope.newGene = {};
            $scope.newMutation = {};
            $scope.newTumorType = {};
            $scope.newTI = [{},{},{},{}];
            $scope.newTrial = '';
            $scope.collaborators = {};
            $scope.checkboxes = {
                'oncogenic': ['YES','NO','Unknown'],
                'mutation_effect': ['Activating','Inactivating', 'Other']
            };
            $scope.nccnDiseaseTypes = ['', "Acute Lymphoblastic Leukemia","Acute Myeloid Leukemia      20th Annual Edition!","Anal Carcinoma","Bladder Cancer","Bone Cancer","Breast Cancer","Cancer of Unknown Primary (See Occult Primary)","Central Nervous System Cancers","Cervical Cancer","Chronic Myelogenous Leukemia","Colon/Rectal Cancer","Colon Cancer      20th Annual Edition!","Rectal Cancer      20th Annual Edition!","Cutaneous Melanoma (See Melanoma)","Endometrial Cancer (See Uterine Neoplasms)","Esophageal and Esophagogastric Junction Cancers","Fallopian Tube Cancer (See Ovarian Cancer)","Gastric Cancer","Head and Neck Cancers","Hepatobiliary Cancers","Hodgkin Lymphoma","Kidney Cancer","Malignant Pleural Mesothelioma","Melanoma","Multiple Myeloma/Other Plasma Cell Neoplasms","Multiple Myeloma","Systemic Light Chain Amyloidosis","Waldenström's Macroglobulinemia / Lymphoplasmacytic Lymphoma","Myelodysplastic Syndromes","Neuroendocrine Tumors","Non-Hodgkin's Lymphomas","Non-Melanoma Skin Cancers","Basal Cell Skin Cancer","Dermatofibrosarcoma Protuberans","Merkel Cell Carcinoma","Squamous Cell Skin Cancer","Non-Small Cell Lung Cancer      20th Annual Edition!","Occult Primary","Ovarian Cancer","Pancreatic Adenocarcinoma","Penile Cancer","Primary Peritoneal Cancer (See Ovarian Cancer)","Prostate Cancer      20th Annual Edition!","Small Cell Lung Cancer      20th Annual Edition!","Soft Tissue Sarcoma","Testicular Cancer","Thymomas and Thymic Carcinomas","Thyroid Carcinoma","Uterine Neoplasms"];
            $scope.nccnCategories = [
              {
                label: '',
                value: ''
              },
              {
                label: 'Category 1: Based upon high-level evidence, there is uniform NCCN consensus that the intervention is appropriate.',
                value: '1'
              },
              {
                label: 'Category 2A: Based upon lower-level evidence, there is uniform NCCN consensus that the intervention is appropriate.',
                value: '2A'
              },
              {
                label: 'Category 2B: Based upon lower-level evidence, there is NCCN consensus that the intervention is appropriate.',
                value: '2B'
              },
              {
                label: 'Category 3: Based upon any level of evidence, there is major NCCN disagreement that the intervention is appropriate.',
                value: '3'
              },
            ]
            $scope.levels = getLevels();
            $scope.fileEditable = false;

            DatabaseConnector.getAllOncoTreeTumorTypes(function(data){
              $scope.tumorTypes = data;
            });

            loadFile().then(function(file){
              $scope.realtimeDocument = file;
              if($scope.fileTitle) {
                var model = $scope.realtimeDocument.getModel();
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
            });

            $scope.$on('$locationChangeStart', function( event , next, current) {
              storage.closeDocument();
            });

            // Get OncoTree primary/secondary/tertiary/quaternary types
            // DatabaseConnector.getAllOncoTreeTumorTypes(function(data){
            //   var tumorTypes = {};

            //   data.forEach(function(e, i){
            //     var key = e.primary;

            //     if(!_.endsWith(key, 'cancer')) {
            //       key += ' cancer';
            //     }

            //     if(tumorTypes.hasOwnProperty(key)) {
            //       var loop=['secondary', 'tertiary', 'quaternary'];
            //       loop.forEach(function(e1, i1){
            //         if(e[e1] && tumorTypes[key].indexOf(e[e1]) === -1) {
            //           tumorTypes[key].push(e[e1]);
            //         }
            //       });
            //     }else {
            //       tumorTypes[key] = [];
            //     }
            //   });

            //   var newTumorTypes = []
            //   for(var key in tumorTypes) {
            //     for(var i = 0; i < tumorTypes[key].length; i++) {
            //       var __datum = {
            //         'name': tumorTypes[key][i],
            //         'tissue': key
            //       }
            //       newTumorTypes.push(__datum);
            //     }
            //   }
            //   $scope.tumorTypes = newTumorTypes;
            // });

        }]
    );
