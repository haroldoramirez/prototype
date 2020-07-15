angular.module('prototype')
    .service('Usuario',['$resource',
        function($resource){
            return $resource('usuario/:id', {}, {
                cadastrar: {method: 'POST', url: 'usuario/cadastrar', isArray: false},
                update: {method: 'PUT', url: 'usuario/:id', isArray: false},
                getAll: {method: 'GET', url: 'usuarios', isArray: true},
                reset: {method: 'POST', url: 'reset/senha', isArray: false},
                getFiltroUsuarios: {method: 'GET', url: 'usuarios/filtro/:filtro', isArray: true},
                getAutenticado: {method: 'GET', url: 'current', isArray: false}
            });
    }]);