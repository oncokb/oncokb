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
                    console.log(target);
                    if (target) {
                        $location.url(target);
                    } else {
                        storage.getDocument('1rFgBCL0ftynBxRl5E6mgNWn0WoBPfLGm8dgvNBaHw38').then(function(file){
                            console.log(file);
                        });
                        // $location.url('/curate');
                    }
                });
            };
        }]
    );
