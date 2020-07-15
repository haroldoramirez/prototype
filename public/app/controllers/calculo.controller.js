angular
    .module('prototype')
    //CONTROLLER INICIO
    .controller('cadastro.controller', function ($scope, $q, $timeout, $state, $stateParams) {

        let defer = $q.defer();
        $scope.loadingCalculos = true;

        $scope.init = function() {
            $scope.loadingCalculos = false;

            console.log("inicio");
        }

    });