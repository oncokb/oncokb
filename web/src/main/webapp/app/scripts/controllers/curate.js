'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:CurateCtrl
 * @description
 * # CurateCtrl
 * Controller of the oncokb
 */
angular.module('oncokb')
    .controller('CurateCtrl', ['$scope', '$location', '$routeParams', 'storage',
        function ($scope, $location, $routeParams, storage) {
            $scope.createDoc = function() {
                if($scope.newDocName) {
                    storage.requireAuth().then(function () {
                        storage.createDocument($scope.newDocName.toString()).then(function (file) {
                            $location.url('/curate/' + file.id + '/');
                        });
                    }, function () {
                        $location.url('/curate');
                    });
                }
            };

            $scope.getDocs = function() {
                storage.requireAuth(true).then(function(){
                    storage.retrieveAllFiles().then(function(result){
                        console.log('Documents', result);
                        $scope.documents = result;
                        // $scope._documents = result;
                        // getDocumentFromList(0, []);
                    });
                });
            };

            $scope.curateDoc = function() {
                console.log($scope);
                console.log($scope.selectedDoc);
                console.log('selected file id', $scope.selectedDoc.id);
                $location.url('/curate/' + $scope.selectedDoc.id + '/');
            };

            $scope.documents = [];
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
    .controller('CurateEditCtrl', ['$scope', '$location', '$routeParams', 'storage', 'realtimeDocument',
        function ($scope, $location, $routeParams, storage, realtimeDocument) {
            $scope.fileId = $routeParams.fileId;
            $scope.realtimeDocument = realtimeDocument;
            $scope.gene = '';
            $scope.newGene = {};
            $scope.newMutation = {};
            $scope.newTumor = {};
            $scope.checkboxes = {
                'oncogenic': ['YES', 'NO', 'N/A']
            };

            if($routeParams.fileId) {
                var model = realtimeDocument.getModel();
                if(!model.getRoot().get('gene')) {
                    var gene = model.create('Gene');
                    model.getRoot().set('gene', gene);
                    $scope.gene = model.getRoot().get('gene');
                }else {
                    $scope.gene = model.getRoot().get('gene');
                }

                console.log($scope.gene);
            }
            $scope.authorize = function(){
                console.log($routeParams);
                console.log('---enter---');
                    storage.requireAuth(false).then(function () {
                    var target = $location.search().target;
                    if (target) {
                        $location.url(target);
                    } else {
                        storage.getDocument('1rFgBCL0ftynBxRl5E6mgNWn0WoBPfLGm8dgvNBaHw38').then(function(file){
                            storage.downloadFile(file).then(function(text) {
                                $scope.curateFile = text;
                            });
                            // var blob = new Blob(['<h1 class="c3 c10 c20"><a name="h.rvs6zqrchald"></a><span class="c0 c5 c23">Gene: PTCH1</span></h1><p class="c3"><span class="c0">Curator name: Dmitriy Zamarin</span></p><p class="c3"><span class="c0">Curator email: zamarind@mskcc.org</span></p>'], {type: 'text/html'});
                            // storage.updateFile(file.id, file, blob).then(function(result){
                            //     console.log(result);
                            // });
                        });
                        // $location.url('/curate');
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
                // console.log(this.newMutation);
                if (this.gene && this.newMutation && this.newMutation.name) {
                    realtimeDocument.getModel().beginCompoundOperation();
                    var mutation = realtimeDocument.getModel().create(OncoKB.Mutation, this.newMutation.name);
                    this.newMutation = {};
                    this.gene.mutations.push(mutation);
                    realtimeDocument.getModel().endCompoundOperation();
                    console.log(this.gene);
                }
            };

            $scope.checkScope = function() {
                console.log($scope.gene.mutations.asArray());
            };

            $scope.remove = function(index, $event) {
                if ($event.stopPropagation) $event.stopPropagation();
                if ($event.preventDefault) $event.preventDefault();
                $scope.gene.mutations.remove(index);
            };

            $scope.$watch('gene', function(newValue, oldValue, scope) {
                console.log(this.gene);
            });
        }]
    )
    .directive("bindCompiledHtml", function($compile, $timeout) {
        return {
            template: '<div></div>',
            scope: {
              rawHtml: '=bindCompiledHtml'
            },
            link: function(scope, elem, attrs) {
              scope.$watch('rawHtml', function(value) {
                if (!value) return;
                // we want to use the scope OUTSIDE of this directive
                // (which itself is an isolate scope).
                var newElem = $compile(value)(scope.$parent);
                elem.contents().remove();
                elem.append(newElem);
              });
            }
        };
    });
