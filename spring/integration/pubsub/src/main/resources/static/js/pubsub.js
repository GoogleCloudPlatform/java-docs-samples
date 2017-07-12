angular.module('pubsub', [])
    .controller('listTopics', function($scope, $http) {
      $http.get('/listTopics').success(function(data) {
        $scope.topics = data;
      });
    })
    .controller('listSubscriptions', function($scope, $http) {
      $http.get('/listSubscriptions').success(function(data) {
        $scope.subscriptions = data;
      })
    });
