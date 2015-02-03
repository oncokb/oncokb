'use strict';

/**
 * @ngdoc function
 * @name oncokb.controller:CurateCtrl
 * @description
 * # CurateCtrl
 * Controller of the oncokb
 */
angular.module('oncokb')
    .controller('CurateCtrl', ['$scope', '$location', 'storage',
        function ($scope, $location, storage) {
            $scope.authorize = function(){
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
