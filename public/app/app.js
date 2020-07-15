angular
    .module
        ('prototype',
            ['ui.router',
             'ngResource',
             'ngAnimate',
             'ui.utils.masks',
             'toastr'
            ]
        )
    .config(function ($stateProvider, $urlRouterProvider, $locationProvider) {

        $locationProvider.html5Mode({enabled: false, requireBase: false, rewriteLinks: false});

        $urlRouterProvider.otherwise("/");

        $stateProvider
            .state('inicio', {
                url: "/",
                templateUrl: 'assets/app/views/inicio.html',
                controller: 'cadastro.controller',
                  ncyBreadcrumb: {
                    label: 'Início'
                  },
                activetab: 'inicio'
            })
    })
    /*Configuracao de loading*/
    .config(function($provide) {
        $provide.decorator('$q', ['$delegate', '$rootScope', function($delegate, $rootScope) {
            let pendingPromisses = 0;
            $rootScope.$watch(
                function() { return pendingPromisses > 0; },
                function(loading) { $rootScope.loading = loading; }
            );
            const $q = $delegate;
            const origDefer = $q.defer;
            $q.defer = function() {
                const defer = origDefer();
                pendingPromisses++;
                defer.promise.finally(function() {
                    pendingPromisses--;
                });
                return defer;
            };
            return $q;
        }])
    /*Configuracao de torrada - mensagens*/
    }).config(function(toastrConfig) {
        angular.extend(toastrConfig, {
            positionClass: 'toast-bottom-center',
            allowHtml: false,
            closeButton: true,
            closeHtml: '<button>&times;</button>',
            extendedTimeOut: 1000,
            iconClasses: {
                error: 'toast-error',
                info: 'toast-info',
                success: 'toast-success',
                warning: 'toast-warning'
            },
            messageClass: 'toast-message',
            onHidden: null,
            onShown: null,
            onTap: null,
            progressBar: true,
            tapToDismiss: true,
            templates: {
            toast: 'directives/toast/toast.html',
            progressbar: 'directives/progressbar/progressbar.html'
      	},
            timeOut: 5000,
            titleClass: 'toast-title',
            toastClass: 'toast'
        });
    })
    // Cache-busting strategy
    .config(['$httpProvider', function($httpProvider) {

        let __version_number = 6.0; // cacheBustSuffix = Date.now('U'); // 'U' -> linux/unix epoch date int

        $httpProvider.interceptors.push(function () {
            return {
                'request': function (config) {
                // !!config.cached represents if the request is resolved using
                // the angular-templatecache
                if (!config.cached) {
                    config.url += ((config.url.indexOf('?')>-1)?'&':'?') + config.paramSerializer({v: __version_number});
                } else if (config.url.indexOf('no-cache') > -1) {
                    // if the cached URL contains 'no-cache' then remove it from the cache
                    config.cache.remove(config.url);
                    config.cached = false; // unknown consequences
                    // Warning: if you remove the value form the cache, and the asset is not
                    // accessable at the given URL, you will get a 404 error.
                }
            return config;
          }
        }
      });
    }]);