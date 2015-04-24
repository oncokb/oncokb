'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:GeneCtrl
 * @description
 * # GeneCtrl
 * Controller of the oncokb
 */
angular.module('oncokbApp')
    .controller('GenesCtrl', ['$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'config', 'importer', 'storage', 'documents', 'users', 'DTColumnDefBuilder', 'DTOptionsBuilder', 'DatabaseConnector', 'OncoKB',
        function ($scope, $rootScope, $location, $timeout, $routeParams, _, config, importer, storage, Documents, users, DTColumnDefBuilder, DTOptionsBuilder, DatabaseConnector, OncoKB) {
            function saveGene(docs, docIndex, callback){
                if(docIndex < docs.length) {
                    var fileId = docs[docIndex].id;
                    storage.getRealtimeDocument(fileId).then(function (realtime){
                        if(realtime && realtime.error) {
                            console.log('did not get realtime document.');
                        }else{
                            console.log(docs[docIndex].title, '\t\t', docIndex);
                            console.log('\t copying');
                            var gene = realtime.getModel().getRoot().get('gene');
                            if(gene) {
                                var geneData = importer.getData(gene);
                                DatabaseConnector.updateGene(JSON.stringify(geneData),
                                    function(result){
                                        console.log('\t success', result);
                                        $timeout(function(){
                                            saveGene(docs, ++docIndex, callback);
                                        }, 200, false);
                                    },
                                    function(result){
                                        console.log('\t failed', result);
                                        $timeout(function(){
                                            saveGene(docs, ++docIndex, callback);
                                        }, 200, false);
                                    }
                                );
                            }else{
                                console.log('\t\tNo gene model.');
                                $timeout(function(){
                                    saveGene(docs, ++docIndex, callback);
                                }, 200, false);
                            }
                        }
                    });
                }else {
                    if(callback) {
                        callback();
                    }
                    console.log('finished.');
                }
            }

            $scope.getDocs = function() {
                var docs = Documents.get();
                if(docs.length > 0) {
                    // $scope.$apply(function() {
                    $scope.documents = Documents.get();
                    $scope.loaded = true;
                    // });
                }else{
                    if(OncoKB.global.genes){
                        storage.requireAuth(true).then(function(){
                            storage.retrieveAllFiles().then(function(result){
                                Documents.set(result);
                                Documents.setGeneStatus(OncoKB.global.genes);
                                $scope.documents = Documents.get();
                                $scope.loaded = true;
                                // loading_screen.finish();
                            });
                        });
                    }else{
                        DatabaseConnector.getAllGene(function(data){
                            OncoKB.global.genes = data;
                            storage.requireAuth(true).then(function(){
                                storage.retrieveAllFiles().then(function(result){
                                    Documents.set(result);
                                    Documents.setStatus(OncoKB.global.genes);
                                    $scope.documents = Documents.get();
                                    $scope.loaded = true;
                                });
                            });
                        });
                    }
                }
            };

            $scope.backup = function() {
                $scope.status.backup = false;
                importer.backup(function(){
                    $scope.status.backup = true;
                });
            };

            $scope.redirect = function(path) {
                $location.path(path);
            };

            $scope.checkError = function() {
                console.log($rootScope.errors);
            };

            $scope.saveAllGenes = function() {
                $scope.status.saveAllGenes = false;
//              console.log($scope.documents);
                saveGene($scope.documents, 0, function(){ $scope.status.saveAllGenes = true;});

//              var sampleGenes = _.slice($scope.documents, 0, 50);
//              saveGene(sampleGenes, 0);
//              saveGene(Documents.get({title: 'U2AF1'}), 0);
            };

            $scope.userRole = users.getMe().role;

            var sorting = [[5, 'desc'], [1, 'asc'], [0, 'asc']];
            if(users.getMe().role === 8) {
                sorting = [[4, 'desc'], [1, 'asc'], [0, 'asc']];
            }

            $scope.dtOptions = DTOptionsBuilder
                .newOptions()
                .withDOM('ifrtlp')
                .withOption('order', sorting)
                .withBootstrap();

            $scope.dtColumns =  [
                DTColumnDefBuilder.newColumnDef(0),
                DTColumnDefBuilder.newColumnDef(1),
                DTColumnDefBuilder.newColumnDef(2).notSortable(),
                DTColumnDefBuilder.newColumnDef(3).notSortable(),
                DTColumnDefBuilder.newColumnDef(4),
                DTColumnDefBuilder.newColumnDef(5),
                DTColumnDefBuilder.newColumnDef(6)
            ];
            $scope.loaded = false;
            $scope.status = {
                backup: true,
                saveAllGenes: true
            };
            $scope.getDocs();
            // var newGenes = ['RET','FGFR4','KIT','SMO','ALK1','ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K1','MET','NOTCH1','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2','RAD21','TET1','TET2','GATA1','GATA2','MPL','B2M','BTK','HLA-A','PRDM1','SF3B1','EPHA7','HIST1H1C','REL','CREBBP','VHL','PBRM1','SETD2','AKT2','AKT3','ERBB3','FGFR1','BRIP1','ERCC4','FANCA','FANCC','EWSR1','MDM2','MDM4','ETV6','DNMT1','EZH2','WT1','EP300','MYD88','CARD11','CD79B','KEAP1','MYC','FH','SDHA','SDHAF2','SDHB','SDHC','SDHD','RASA1','FOXA1','AURKA','AURKB','CCND1','CCNE1','PPP2R1A','CCND2','CCND3','PAK1','ERG','ETV1','SPOP','SOX2','MYOD1','CTCF','MTOR','PDGFRA','DAXX','MLH1','MSH6','RAD54L','RECQL4','BCL2','RB1','EIF1AX','EIF4A2','EIF4E','EPCAM','FAT1','SMAD2','SMAD3','SMAD4','TGFBR1','TGFBR2','U2AF1','BMPR1A','XPO1','ATRX','SMARCB1','CD276','CD274','CTLA4','TNFRSF14','IL7R','JAK1','JAK2','JAK3','IKZF1','ACVR1','IGF1','IGF1R','IGF2','INHA','INHBA','BAP1','NF2','RAF1','CDKN1B','RHOA','RAC1','MEN1','CDH1','STAG2','MDC1','MRE11A','POLD1','SOX9','ZFHX3','NF1','RAD50','RAD51B','RAD51C','RAD51D','RAD52','TOP1','HIST1H3A','KDM5C','KDM6A','KMT2A','KMT2C','KMT2D','H3F3A','H3F3B','H3F3C','KDM5A','POLE','DNMT3A','DNMT3B','AR','E2F3','FOXP1','RYBP','SHQ1','PAX8','TCEB1','CDKN2A','SMARCA4','TERT','BRCA1','BRCA2','PALB2','RHEB','TSC1','TSC2','RICTOR','ARID1A','ERCC2','CIC','FUBP1','HNF1A','MED12','BCOR','YAP1','LATS1','LATS2','MST1','ESR1','ATM','CHEK1','CHEK2','ATR','CENPA','MITF','FLT3','CEBPA','NPM1','RBM10','APC','ARAF','ARID1B','ARID2','ARID5B','BCL2L11','BRD4','CASP8','CBL','CDK12','CDK4','CDK6','CDKN1A','CDKN2B','GATA3','KDR','MAP2K2','MAP2K4','MAP3K1','MAX','MSH2','MYCN','NFE2L2','NFKBIA','NTRK1','NTRK2','NTRK3','PIK3CB','PIK3R2','PMS2','PTPN11','PTPRD','ROS1','RUNX1','SRC','TMPRSS2','XRCC2'];
            // var newGenes = ['ERBB2','PIK3CA','PIK3R1','PTEN','DDR2','FGFR3','IDH1','KRAS','MAP2K1','MET','NOTCH1','NRAS','AKT1','FBXW7','FGFR2','FOXL2','GNA11','GNAQ','GNAS','HRAS','PDGFRB','PTCH1','STK11','CTNNB1','ERBB4','IDH2'];
            var newGenes = ['GLI1', 'ETV4'];

            //phase 1/2 genes 210
            // var newGenes = ['RAD21','MSH2','XRCC2','TET1','TET2','GATA1','GATA2','MPL','CBL','KDR','PTPN11','B2M','BTK','HLA-A','PRDM1','SF3B1','EPHA7','HIST1H1C','REL','CREBBP','VHL','PBRM1','SETD2','AKT2','AKT3','ERBB3','FGFR1','BRIP1','ERCC4','FANCA','FANCC','EWSR1','MDM2','MDM4','ETV6','DNMT1','EZH2','WT1','ROS1','EP300','MYD88','CARD11','CD79B','KEAP1','NFE2L2','MYC','FH','SDHA','SDHAF2','BRD4','MAX','MYCN','RASA1','MAP2K2','MAP2K4','MAP3K1','FOXA1','AURKA','AURKB','CCND1','CCNE1','PPP2R1A','CCND2','CCND3','PAK1','ERG','ETV1','SPOP','TMPRSS2','SOX2','KMT2C','MYOD1','SRC','CTCF','CDKN1A','CDKN2B','APC','NTRK1','NTRK2','NTRK3','CDK4','CDK6','NFKBIA','MTOR','PDGFRA','DAXX','MLH1','MSH6','RAD54L','RECQL4','PMS2','BCL2L11','BCL2','RB1','EIF1AX','EIF4A2','EIF4E','GATA3','EPCAM','FAT1','SMAD2','SMAD3','SMAD4','TGFBR1','TGFBR2','U2AF1','BMPR1A','XPO1','ATRX','SMARCB1','CD276','CD274','CTLA4','TNFRSF14','IL7R','JAK1','JAK2','JAK3','IKZF1','ACVR1','IGF1','IGF1R','IGF2','INHA','INHBA','BAP1','NF2','RAF1','CDKN1B','RHOA','RAC1','MEN1','ARAF','CDH1','STAG2','MDC1','MRE11A','POLD1','SOX9','ZFHX3','NF1','CASP8','RAD50','RAD51B','RAD51C','RAD51D','RAD52','TOP1','HIST1H3A','KDM5C','KDM6A','KMT2A','KMT2D','H3F3A','H3F3B','H3F3C','KDM5A','POLE','PTPRD','DNMT3A','DNMT3B','RUNX1','AR','E2F3','FOXP1','RYBP','SHQ1','PIK3CB','PIK3R2','SDHB','SDHC','SDHD','PAX8','TCEB1','CDKN2A','SMARCA4','TERT','BRCA1','BRCA2','PALB2','CDK12','RHEB','TSC1','TSC2','RICTOR','ARID1A','ERCC2','ARID1B','ARID2','ARID5B','CIC','FUBP1','HNF1A','MED12','BCOR','YAP1','LATS1','LATS2','MST1','ESR1','ATM','CHEK1','CHEK2','ATR','CENPA','MITF','FLT3','CEBPA','NPM1','RBM10'];
            // first 50 genes
            // var newGenes = ['RAD21','MSH2','XRCC2','TET1','TET2','GATA1','GATA2','MPL','CBL','KDR','PTPN11','B2M','BTK','HLA-A','PRDM1','SF3B1','EPHA7','HIST1H1C','REL','CREBBP','VHL','PBRM1','SETD2','AKT2','AKT3','ERBB3','FGFR1','BRIP1','ERCC4','FANCA','FANCC','EWSR1','MDM2','MDM4','ETV6','DNMT1','EZH2','WT1','ROS1','EP300','MYD88','CARD11','CD79B','KEAP1','NFE2L2','MYC','FH','SDHA','SDHAF2','BRD4'];
            // Second 50 genes
            // var newGenes = ['MAX','MYCN','RASA1','MAP2K2','MAP2K4','MAP3K1','FOXA1','AURKA','AURKB','CCND1','CCNE1','PPP2R1A','CCND2','CCND3','PAK1','ERG','ETV1','SPOP','TMPRSS2','SOX2','KMT2C','MYOD1','SRC','CTCF','CDKN1A','CDKN2B','APC','NTRK1','NTRK2','NTRK3','CDK4','CDK6','NFKBIA','MTOR','PDGFRA','DAXX','MLH1','MSH6','RAD54L','RECQL4','PMS2','BCL2L11','BCL2','RB1','EIF1AX','EIF4A2','EIF4E','GATA3','EPCAM','FAT1'];
            // rest 110 genes
            // var newGenes = ['SMAD2','SMAD3','SMAD4','TGFBR1','TGFBR2','U2AF1','BMPR1A','XPO1','ATRX','SMARCB1','CD276','CD274','CTLA4','TNFRSF14','IL7R','JAK1','JAK2','JAK3','IKZF1','ACVR1','IGF1','IGF1R','IGF2','INHA','INHBA','BAP1','NF2','RAF1','CDKN1B','RHOA','RAC1','MEN1','ARAF','CDH1','STAG2','MDC1','MRE11A','POLD1','SOX9','ZFHX3','NF1','CASP8','RAD50','RAD51B','RAD51C','RAD51D','RAD52','TOP1','HIST1H3A','KDM5C','KDM6A','KMT2A','KMT2D','H3F3A','H3F3B','H3F3C','KDM5A','POLE','PTPRD','DNMT3A','DNMT3B','RUNX1','AR','E2F3','FOXP1','RYBP','SHQ1','PIK3CB','PIK3R2','SDHB','SDHC','SDHD','PAX8','TCEB1','CDKN2A','SMARCA4','TERT','BRCA1','BRCA2','PALB2','CDK12','RHEB','TSC1','TSC2','RICTOR','ARID1A','ERCC2','ARID1B','ARID2','ARID5B','CIC','FUBP1','HNF1A','MED12','BCOR','YAP1','LATS1','LATS2','MST1','ESR1','ATM','CHEK1','CHEK2','ATR','CENPA','MITF','FLT3','CEBPA','NPM1','RBM10'];

            $scope.migrate = function(){
                console.log($scope.documents);

                //Steo 0. Create a folder to put all files (Manully did)
                var newFolderId = '0BzBfo69g8fP6fjVqN1dCbGdYRUYxd0xfck5FTjdWLXVCaEZxZjhuWTFIcFBVODk3ZDNEX0E';
                //Step 1. For loop all documents
                //Step 2. Create file with same name
                //Step 3. Give file same permission
                //Step 4. Copy file content, but create

                storage.getPermission($scope.documents[0].id).then(function(result){
                    console.log(result);
                });
            };

            $scope.create = function() {
                createDoc(0);
            };

            $scope.givePermission = function() {
                var testGene = {};
                var genes = [];

                for(var key in testGene) {
                    /* jshint -W083 */
                    var _genes = testGene[key].trim().split(',').map(function(e){ return e.trim();});
                    _genes.forEach(function(_gene){
                        genes.push({'email': key, 'gene': _gene});
                    });
                    /* jshint +W083 */
                }

                $scope.genesPermissions = genes;
                givePermissionSub(0);
            };

            function givePermissionSub(index){
                if(index < $scope.genesPermissions.length) {
                    var permission = $scope.genesPermissions[index];
                    console.log(permission.gene, '\t', permission.email);
                    var _docs = Documents.get({title: permission.gene});
                    var _doc = _docs[0];
                    if(_doc.id) {
                        storage.requireAuth().then(function(){
                            storage.getPermission(_doc.id).then(function(result){
                                if(result.items && angular.isArray(result.items)) {
                                    var permissionIndex = -1;
                                    result.items.forEach(function(permission, _index){
                                        if(permission.emailAddress && permission.emailAddress === permission.email) {
                                            permissionIndex = _index;
                                        }
                                    });

                                    if(permissionIndex === -1) {
                                        storage.insertPermission(_doc.id, permission.email, 'user', 'writer').then(function(result){
                                            if(result && result.error) {
                                                console.log('Error when insert permission.');
                                            }else{
                                                console.log('\tinsert writer to', permission.gene);
                                                givePermissionSub(++index);
                                            }
                                        });
                                    }else if(result.items[permissionIndex].role !== 'writer'){
                                        storage.updatePermission(_doc.id, result.items[permissionIndex].id, 'writer').then(function(result){
                                            if(result && result.error) {
                                                console.log('Error when update permission.');
                                            }else{
                                                console.log('\tupdat  writer to', permission.gene);
                                                givePermissionSub(++index);
                                            }
                                        });
                                    }
                                }
                            });
                        });
                    }
                }else{
                    console.info('Done.....');
                }
            }

            $scope.giveFolderPermission = function() {
                var emails = ['cbioportal@gmail.com'];
                var folderId = config.folderId;

                emails.forEach(function(email){
                    storage.requireAuth(true).then(function(){
                        storage.getDocument(folderId).then(function(e1){
                            if(e1.id) {
                                storage.getPermission(e1.id).then(function(result){
                                    if(result.items && angular.isArray(result.items)) {
                                        var permissionIndex = -1;
                                        result.items.forEach(function(permission, _index){
                                            if(permission.emailAddress && permission.emailAddress === email) {
                                                permissionIndex = _index;
                                            }
                                        });

                                        console.log(permissionIndex);
                                        // if(permissionIndex === -1) {
                                        //   storage.insertPermission(e1.id, key, 'user', 'writer').then(function(result){
                                        //     console.log('insert result', result);
                                        //   });
                                        // }else if(result.items[permissionIndex].role !== 'writer'){
                                        //   storage.updatePermission(e1.id, result.items[permissionIndex].id, 'writer').then(function(result){
                                        //     console.log('update result', result);
                                        //   });
                                        // }
                                    }
                                });
                            }
                        });
                    });
                });
            };

            function createDoc(index) {
                if(index < newGenes.length) {
                    storage.requireAuth().then(function () {
                        console.log(index, ' -> Creating', newGenes[index]);
                        // storage.createDocument(newGenes[index], '0BzBfo69g8fP6Mnk3RjVrZ0pJX3M').then(function (file) {
                        storage.createDocument(newGenes[index]).then(function (result) {
                            if(result && result.error) {
                                console.log('Error when creating docuemnt.');
                            }else {
                                $timeout(function(){
                                    createDoc(++index);
                                }, 2000);
                            }
                        });
                    });
                }else {
                    console.log('finished');
                }
            }
        }]
)
    .controller('GeneCtrl', ['_', 'S', '$resource', '$interval', '$timeout', '$scope', '$rootScope', '$location', '$route', '$routeParams', 'dialogs', 'importer', 'driveOncokbInfo', 'storage', 'loadFile', 'user', 'users', 'documents', 'OncoKB', 'gapi', 'DatabaseConnector', 'SecretEmptyKey', 'jspdf',
        function (_, S, $resource, $interval, $timeout, $scope, $rootScope, $location, $route, $routeParams, dialogs, importer, DriveOncokbInfo, storage, loadFile, User, Users, Documents, OncoKB, gapi, DatabaseConnector, SecretEmptyKey, jspdf) {
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

            $scope.addMutation = function(newMutationName) {
                if (this.gene && newMutationName) {
                    newMutationName = newMutationName.toString().trim();
                    var exists = false;
                    this.gene.mutations.asArray().forEach(function(e){
                        if(e.name.getText().toLowerCase() === newMutationName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if(exists) {
                        dialogs.notify('Warning', 'Mutation exists.');
                    }else{
                        var _mutation = '';
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _mutation = $scope.realtimeDocument.getModel().create(OncoKB.Mutation);
                        _mutation.name.setText(newMutationName);
                        this.gene.mutations.push(_mutation);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[this.gene.mutations.length - 1] = {
                            'isOpen': true
                        };
                        sendEmail(this.gene.name.text + ': new MUTATION added -> ' + newMutationName, ' ');
                    }
                }
            };

            $scope.stateComparator = function (state, viewValue) {
                return viewValue === SecretEmptyKey || (''+state).toLowerCase().indexOf((''+viewValue).toLowerCase()) > -1;
            };

            $scope.getComments = function() {
                console.log($scope.comments);
            };

            $scope.addComment = function(object, key, string) {
                var _user = Users.getMe();
                if (object && object[key+'_comments'] && _user.email) {
                    var _comment = '';
                    var _date = new Date();

                    $scope.realtimeDocument.getModel().beginCompoundOperation();
                    _comment = $scope.realtimeDocument.getModel().create('Comment');
                    _comment.date.setText(_date.getTime().toString());
                    if(_user.name) {
                        _comment.userName.setText(_user.name);
                    }else {
                        _comment.userName.setText('Unknown');
                    }
                    _comment.email.setText(_user.email);
                    _comment.content.setText(string);
                    _comment.resolved.setText('false');
                    object[key+'_comments'].push(_comment);
                    $scope.realtimeDocument.getModel().endCompoundOperation();
                }else {
                    console.log('Unable to add comment.');
                }
            };

            $scope.getData = function() {
                var gene = importer.getData(this.gene);
                console.log(gene);
                console.log(JSON.stringify(gene));
            };

            $scope.updateGene = function() {
                $scope.docStatus.savedGene = false;
                var gene = importer.getData(this.gene);
                // $timeout(function(){
                DatabaseConnector.updateGene(JSON.stringify(gene), function(result){ $scope.docStatus.savedGene = true; console.log('success', result);}, function(result){ $scope.docStatus.savedGene = true; console.log('failed', result);});
                // }, 1000);
            };

            $scope.addTumorType = function(mutation, newTumorTypeName, mutationIndex) {
                if (mutation && newTumorTypeName) {
                    var _tumorType = '';
                    var exists = false;
                    newTumorTypeName = newTumorTypeName.toString().trim();

                    mutation.tumors.asArray().forEach(function(e){
                        if(e.name.getText().toLowerCase() === newTumorTypeName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if(exists) {
                        dialogs.notify('Warning', 'Tumor type exists.');
                    }else{
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _tumorType = $scope.realtimeDocument.getModel().create(OncoKB.Tumor);
                        _tumorType.name.setText(newTumorTypeName);
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
                        $scope.geneStatus[mutationIndex][mutation.tumors.length - 1] = {
                            'isOpen': true
                        };
                        sendEmail(this.gene.name.text + ',' + mutation.name.text + ' new TUMOR TYPE added -> ' + newTumorTypeName, ' ');
                    }
                }
            };

            //Add new therapeutic implication
            $scope.addTI = function(ti, index, newTIName, mutationIndex, tumorIndex, tiIndex) {
                if (ti && newTIName) {
                    var _treatment = '';
                    var exists = false;
                    newTIName = newTIName.toString().trim();

                    ti.treatments.asArray().forEach(function(e){
                        if(e.name.getText().toLowerCase() === newTIName.toLowerCase()) {
                            exists = true;
                        }
                    });

                    if(exists) {
                        dialogs.notify('Warning', 'Therapy exists.');
                    }else{
                        $scope.realtimeDocument.getModel().beginCompoundOperation();
                        _treatment = $scope.realtimeDocument.getModel().create(OncoKB.Treatment);
                        _treatment.name.setText(newTIName);
                        _treatment.type.setText('Therapy');
                        if($scope.checkTI(ti, 1, 1)) {
                            _treatment.level.setText('1');
                        }else if($scope.checkTI(ti, 0, 1)){
                            _treatment.level.setText('4');
                        }else if($scope.checkTI(ti, 1, 0)){
                            _treatment.level.setText('1');
                        }else if($scope.checkTI(ti, 0, 0)){
                            _treatment.level.setText('4');
                        }
                        ti.treatments.push(_treatment);
                        $scope.realtimeDocument.getModel().endCompoundOperation();
                        $scope.geneStatus[mutationIndex][tumorIndex][tiIndex][ti.treatments.length - 1].isOpen = true;
                    }
                }
            };

            $scope.onFocus = function (e) {
                $timeout(function () {
                    /* jshint -W117 */
                    $(e.target).trigger('input');
                    $(e.target).trigger('change'); // for IE
                    /* jshint +W117 */
                });
            };

            //Add new therapeutic implication
            $scope.addTrial = function(trials, newTrial) {
                if (trials && newTrial) {
                    if(trials.indexOf(newTrial) === -1){
                        if(newTrial.match(/NCT[0-9]+/ig)) {
                            trials.push(newTrial);
                        }else{
                            dialogs.notify('Warning', 'Please check your trial ID format. (e.g. NCT01562899)');
                        }
                    }else {
                        dialogs.notify('Warning', 'Trial exists.');
                    }
                }
            };

            $scope.cleanTrial = function(trials) {
                var cleanTrials = {};
                trials.asArray().forEach(function(e,index){
                    if(cleanTrials.hasOwnProperty(e)){
                        cleanTrials[e].push(index);
                    }else{
                        cleanTrials[e] = [];
                    }
                });
                /*jshint -W083 */
                for(var key in cleanTrials) {
                    if(cleanTrials[key].length > 0) {
                        cleanTrials[key].forEach(function(e){
                            trials.removeValue(key);
                        });
                    }
                }
                /*jshint +W083 */
                console.log(cleanTrials);
            };

            $scope.addTrialStr = function(trials) {
                if (trials && this.trialsStr) {
                    var _trials = this.trialsStr.split(/\s+/);
                    _trials.forEach(function(e){
                        if(trials.indexOf(e) === -1) {
                            trials.push(e);
                        }
                    });
                    this.trialsStr = '';
                }
            };

            $scope.checkScope = function() {
                console.log($scope.expandAll);
                console.log($scope.gene);
                console.log($scope.gene.mutations.get(0).tumors.get(0));
                console.log($scope.geneStatus);
            };

            $scope.remove = function(index, object, event) {
                $scope.stopCollopse(event);
                var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to delete this entry?');
                dlg.result.then(function(){
                    object.remove(index);
                },function(){});
            };

            $scope.commentClick = function(event) {
                $scope.stopCollopse(event);
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

            $scope.move = function(driveList, index, moveIndex, event) {
                $scope.stopCollopse(event);

                index = parseInt(index);
                moveIndex = parseInt(moveIndex);

                if(moveIndex <= index) {
                    moveIndex = moveIndex <=0 ? 0 : moveIndex-1;
                }

                moveIndex = moveIndex < driveList.length ? moveIndex : driveList.length;

                driveList.move(index, moveIndex);
            };

            $scope.stopCollopse = function(event) {
                if (event.stopPropagation) { event.stopPropagation();}
                if (event.preventDefault) { event.preventDefault();}
            };

            $scope.setGeneStatus = function(){
                var newStatus = {
                    geneId: $scope.gene.name.text,
                    status: $scope.gene.status.text
                };
                Documents.updateStatus(newStatus);
                DatabaseConnector.setGeneStatus(newStatus).then(function(result){
                    if(result && result.error){
                        console.error(result);
                    }else{
                        console.info(result);
                    }
                });
            };

            $scope.generatePDF = function() {
                jspdf.create(importer.getData(this.gene));
            };

            $scope.isOpenFunc = function(type){
                if(type === 'expand'){
                    $scope.expandAll = true;
                }else if (type === 'collapse'){
                    $scope.expandAll = false;
                }

                //for: mutation
                for(var key in $scope.geneStatus) {
                    if(!isNaN(key)){
                        $scope.geneStatus[key].isOpen = $scope.expandAll;
                    }

                    //for: tumor type
                    for(var _key in $scope.geneStatus[key]) {
                        //for: therapeutic implications
                        if(_key !== 'isOpen'){
                            var flag = $scope.expandAll;
                            if(isNaN(_key) && flag){
                                flag = $scope.gene.mutations.get(Number(key))[_key].text?$scope.expandAll: false;
                            }
                            $scope.geneStatus[key][_key].isOpen = flag;


                            for(var __key in $scope.geneStatus[key][_key]) {
                                var flag = $scope.expandAll;
                                if(__key !== 'isOpen'){
                                    if(isNaN(__key) && flag){
                                        if(__key === 'nccn'){
                                            flag = $scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).disease?$scope.expandAll: false;
                                        }else if(__key === 'trials'){
                                            flag = $scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).trials.length > 0?$scope.expandAll: false;
                                        }else{
                                            flag = $scope.gene.mutations.get(Number(key)).tumors.get(Number(_key))[__key].text?$scope.expandAll: false;
                                        }
                                        $scope.geneStatus[key][_key][__key].isOpen = flag;
                                    }else if(!isNaN(__key)){
                                        if($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).TI.get(Number(__key)).treatments.length > 0){
                                            //for: treatments
                                            $scope.geneStatus[key][_key][__key].isOpen = flag;
                                            for(var ___key in $scope.geneStatus[key][_key][__key]) {
                                                if(___key !== 'isOpen'){
                                                    $scope.geneStatus[key][_key][__key][___key].isOpen = flag;
                                                }
                                            }
                                        }else if($scope.gene.mutations.get(Number(key)).tumors.get(Number(_key)).TI.get(Number(__key)).description.text){
                                            $scope.geneStatus[key][_key][__key].isOpen = flag;
                                        }else{
                                            $scope.geneStatus[key][_key][__key].isOpen = false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };

            $scope.changeIsOpen = function(target){
                console.log(target);
                target = !target;
            };

            function sendEmail(subject, content) {
                if($scope.userRole < 8) {
                    var param = {subject: subject, content: content};

                    DatabaseConnector.sendEmail(
                        param,
                        function(result){ console.log('success', result);},
                        function(result){ console.log('failed', result);}
                    );
                }
            }

            function getDriveOncokbInfo() {
                var pubMedLinks = DriveOncokbInfo.getPubMed({gene: $scope.fileTitle});
                var pubMedLinksLength = 0;
                $scope.suggestedMutations = DriveOncokbInfo.getMutation($scope.fileTitle) || [];
                if($scope.suggestedMutations.length === 0) {
                    $scope.addMutationPlaceholder = 'Based on our search criteria no hotspot mutation found. Please curate according to literature.';
                }

                $scope.pubMedLinks = {
                    gene: pubMedLinks.gene.pubMedLinks || [],
                    mutations: pubMedLinks.mutations.pubMedMutationLinks || {}
                };

                for(var key in $scope.pubMedLinks.mutations) {
                    pubMedLinksLength += $scope.pubMedLinks.mutations[key].length;
                }
                $scope.pubMedMutationLength = Object.keys($scope.pubMedLinks.mutations).length;
                $scope.pubMedMutationLinksLength = pubMedLinksLength;
                $scope.pubMedLinksLength = pubMedLinksLength + $scope.pubMedLinks.gene.length;
            }

            function bindDocEvents() {
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_JOINED, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.COLLABORATOR_LEFT, displayCollaboratorEvent);
                $scope.realtimeDocument.addEventListener(gapi.drive.realtime.EventType.DOCUMENT_SAVE_STATE_CHANGED, saveStateChangedEvent);
                $scope.model.addEventListener(gapi.drive.realtime.EventType.UNDO_REDO_STATE_CHANGED, onUndoStateChanged);
                $scope.gene.addEventListener(gapi.drive.realtime.EventType.VALUE_CHANGED, valueChangedEvent);
            }

            function saveStateChangedEvent(evt) {
                if(!$scope.$$phase) {
                    $scope.$apply(function(){
                        updateDocStatus(evt);
                    });
                }else {
                    updateDocStatus(evt);
                }
            }

            function updateDocStatus(evt) {
                if(evt.isSaving){
                    documentSaving();
                }else if(!evt.isSaving && !evt.currentTarget.isClosed){
                    documentSaved();
                }else {
                    documentClosed();
                }
            }

            function afterCreateGeneModel() {
                var file = Documents.get({title: $scope.fileTitle});
                file = file[0];
                $scope.document = file;
                $scope.fileEditable = file.editable?true:false;
                $scope.loaded = true;
                displayAllCollaborators($scope.realtimeDocument, bindDocEvents);
            }

            function valueChangedEvent(evt) {
                console.log('valueChanged', evt);
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

            function documentSaving() {
                $scope.docStatus.saving = true;
                $scope.docStatus.saved = false;
                $scope.docStatus.closed = false;
            }

            function documentSaved() {
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = true;
                $scope.docStatus.closed = false;
            }

            function documentClosed(){
                $scope.docStatus.closed = true;
                $scope.docStatus.saving = false;
                $scope.docStatus.saved = false;
                $scope.fileEditable = false;
            }

            function getOncoTreeTumortypes(){
                $scope.tumorTypes = [ 'Adrenocortical Carcinoma',
                    'Pheochromocytoma',
                    'Ampullary Carcinoma',
                    'Biliary Cancer',
                    'Bladder Cancer',
                    'Blastic Plasmacytoid Dendritic Cell Neoplasm',
                    'Histiocytosis',
                    'Leukemia',
                    'Multiple Myeloma',
                    'Myelodysplasia',
                    'Myeloproliferative Neoplasm',
                    'Chondroblastoma',
                    'Chondrosarcoma',
                    'Chordoma',
                    'Ewing Sarcoma',
                    'Giant Cell Tumor',
                    'Osteosarcoma',
                    'Anal Cancer',
                    'Melanoma',
                    'Appendiceal Cancer',
                    'Colorectal Cancer',
                    'Gastrointestinal Neuroendocrine Tumor',
                    'Small Bowel Cancer',
                    'Diffuse Glioma',
                    'Encapsulated Glioma',
                    'Ependymomal Tumor',
                    'Miscellaneous Neuroepithelial Tumor',
                    'Meningothelial Tumor',
                    'Embryonal Tumor',
                    'Sellar Tumor',
                    'Nerve Sheath Tumor',
                    'Choroid Plexus Tumor',
                    'Pineal Tumor',
                    'Germ Cell Tumor',
                    'Miscellaneous Brain Tumor',
                    'Breast Carcinoma',
                    'Cervical Cancer',
                    'Esophagogastric Carcinoma',
                    'Retinoblastoma',
                    'Head and Neck Carcinoma',
                    'Renal Cell Carcinoma',
                    'Wilms Tumor',
                    'Hepatocellular Carcinoma',
                    'Hodgkin\'s Lymphoma',
                    'Non-Hodgkin’s Lymphoma',
                    'Lung cancer',
                    'Mesothelioma',
                    'Ovarian Cancer',
                    'Pancreatic Cancer',
                    'Penile Cancer',
                    'Prostate Cancer',
                    'Skin Cancer, Non-Melanoma',
                    'Soft Tissue Sarcoma',
                    'Gastrointestinal Stromal Tumor',
                    'Thymic Tumor',
                    'Thyroid Cancer',
                    'Gestational Trophoblastic Disease',
                    'Endometrial Cancer',
                    'Uterine Sarcoma',
                    'Vulvar Carcinoma',
                    'Cancer of Unknown Primary',
                    'Mixed Cancer Types',
                    'Unknown Cancer Type' ];
            }

            function getLevels() {
                var desS = {
                    '': '',
                    '1': 'FDA-approved biomarker and drug association in this indication.',
                    '2A': 'FDA-approved biomarker and drug association in another indication, and NCCN-compendium listed for this indication.',
                    '2B': 'FDA-approved biomarker in another indication, but not FDA or NCCN-compendium-listed for this indication.',
                    '3': 'Clinical evidence links this biomarker to drug response but no FDA-approved or NCCN compendium-listed biomarker and drug association.',
                    '4': 'Preclinical evidence potentially links this biomarker to response but no FDA-approved or NCCN compendium-listed biomarker and drug association.'
                };

                var desR = {
                    '': '',
                    'R1': 'NCCN-compendium listed biomarker for resistance to a FDA-approved drug.',
                    'R2': 'Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance.',
                    'R3': 'Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance.'
                };

                var levels = {};

                var levelsCategories = {
                    SS: ['','1','2A'],
                    SR: ['R1'],
                    IS: ['','2B','3','4'],
                    IR: ['R2','R3']
                };

                for(var key in levelsCategories) {
                    var _items = levelsCategories[key];
                    levels[key] = [];
                    for (var i = 0; i < _items.length; i++) {
                        var __datum = {};
                        __datum.label = _items[i] + (_items[i]===''?'':' - ') +( (['SS', 'IS'].indexOf(key) !== -1) ? desS[_items[i]] : desR[_items[i]]);
                        __datum.value = _items[i];
                        levels[key].push(__datum);
                    }
                }
                return levels;
            }

            $scope.loaded = false;
            $scope.fileTitle = $routeParams.geneName;
            $scope.gene = '';
            $scope.comments = '';
            $scope.newGene = {};
            $scope.collaborators = {};
            $scope.checkboxes = {
                'oncogenic': ['YES','LIKELY', 'NO', 'UNKNOWN'],
                'mutation_effect': ['Activating','Inactivating', 'Other'],
                'geneStatus': ['Complete', 'Proceed with caution', 'Not ready']
            };
            $scope.nccnDiseaseTypes = ['', 'Acute Lymphoblastic Leukemia','Acute Myeloid Leukemia      20th Annual Edition!','Anal Carcinoma','Bladder Cancer','Bone Cancer','Breast Cancer','Cancer of Unknown Primary (See Occult Primary)','Central Nervous System Cancers','Cervical Cancer','Chronic Myelogenous Leukemia','Colon/Rectal Cancer','Colon Cancer      20th Annual Edition!','Rectal Cancer      20th Annual Edition!','Cutaneous Melanoma (See Melanoma)','Endometrial Cancer (See Uterine Neoplasms)','Esophageal and Esophagogastric Junction Cancers','Fallopian Tube Cancer (See Ovarian Cancer)','Gastric Cancer','Head and Neck Cancers','Hepatobiliary Cancers','Hodgkin Lymphoma','Kidney Cancer','Malignant Pleural Mesothelioma','Melanoma','Multiple Myeloma/Other Plasma Cell Neoplasms','Multiple Myeloma','Systemic Light Chain Amyloidosis','Waldenström\'s Macroglobulinemia / Lymphoplasmacytic Lymphoma','Myelodysplastic Syndromes','Neuroendocrine Tumors','Non-Hodgkin\'s Lymphomas','Non-Melanoma Skin Cancers','Basal Cell Skin Cancer','Dermatofibrosarcoma Protuberans','Merkel Cell Carcinoma','Squamous Cell Skin Cancer','Non-Small Cell Lung Cancer      20th Annual Edition!','Occult Primary','Ovarian Cancer','Pancreatic Adenocarcinoma','Penile Cancer','Primary Peritoneal Cancer (See Ovarian Cancer)','Prostate Cancer      20th Annual Edition!','Small Cell Lung Cancer      20th Annual Edition!','Soft Tissue Sarcoma','Testicular Cancer','Thymomas and Thymic Carcinomas','Thyroid Carcinoma','Uterine Neoplasms'];
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
            ];
            $scope.levels = getLevels();
            $scope.fileEditable = false;
            $scope.docStatus = {
                saved: true,
                saving: false,
                closed: false,
                savedGene: true
            };
            $scope.addMutationPlaceholder = 'Mutation Name';
            $scope.userRole = Users.getMe().role;
            $scope.levelExps = {
                SR: '<strong>Level R1:</strong> NCCN-compendium listed biomarker for resistance to a FDA-approved drug.<br/>Example 1: Colorectal cancer with KRAS mutation → resistance to cetuximab<br/>Example 2: EGFR-L858R or exon 19 mutant lung cancers with coincident T790M mutation → resistance to erlotinib',
                IR: '<strong>Level R2:</strong> Not NCCN compendium-listed biomarker, but clinical evidence linking this biomarker to drug resistance.<br/>Example: Resistance to crizotinib in a patient with metastatic lung adenocarcinoma harboring a CD74-ROS1 rearrangement (PMID: 23724914).<br/><strong>Level R3:</strong> Not NCCN compendium-listed biomarker, but preclinical evidence potentially linking this biomarker to drug resistance.<br/>Example: Preclinical evidence suggests that BRAF V600E mutant thyroid tumors are insensitive to RAF inhibitors (PMID: 23365119).<br/>'
            };
            $scope.showHideButtons = [
                {'key': 'prevelenceShow', 'display': 'Prevalence'},
                {'key': 'proImShow', 'display': 'Prognostic implications'},
                {'key': 'nccnShow', 'display': 'NCCN guidelines'},
                {'key': 'ssShow', 'display': 'Standard implications for sensitivity to therapy'},
                {'key': 'srShow', 'display': 'Standard implications for resistance to therapy'},
                {'key': 'isShow', 'display': 'Investigational implications for sensitivity to therapy'},
                {'key': 'irShow', 'display': 'Investigational implications for resistance to therapy'},
                {'key': 'trialsShow', 'display': 'Ongoing clinical trials'}
            ];
            $scope.list = [];
            $scope.sortableOptions = {
                stop: function(e, ui){
                    console.log('dropindex',ui.dropindex);
                    console.log('index',ui.index);
                    console.log(e, ui);
                },
                beforeStop: function(e, ui){
                    console.log('dropindex',ui.dropindex);
                    console.log('index',ui.index);
                    console.log(e, ui);
                }
                // handle: '> .myHandle'
            };
            $scope.selfParams = {};
            $scope.geneStatus = {};

            $scope.expandAll = false;

            getDriveOncokbInfo();
            getOncoTreeTumortypes();
            var clock;
            clock = $interval(function() {
                storage.requireAuth(true).then(function(result){
                    if(result && !result.error) {
                        console.log('\t checked token', new Date().getTime());
                    }else {
                        documentClosed();
                        console.log('error when renew token in interval func.');
                    }
                });
            }, 600000);


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


            // Token expired, refresh
            $rootScope.$on('realtimeDoc.token_refresh_required', function () {
                console.log('--token_refresh_required-- going to refresh page.');
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Other unidentify error
            $rootScope.$on('realtimeDoc.other_error', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.client_error', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            // Realtime documet not found
            $rootScope.$on('realtimeDoc.not_found', function () {
                dialogs.error('Error', 'An error has occurred. This page will be redirected to genes page.');
                documentClosed();
                $location.path('/genes');
            });

            $scope.$on('$locationChangeStart', function() {
                storage.closeDocument();
                documentClosed();
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
