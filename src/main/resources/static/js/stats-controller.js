app.factory('StatsResource', function ($resource) {
    return $resource('/citation/car/manufacturer/count');
});
app.controller('StatsCtrl', function (NgMap, $resource, $scope, StatsResource) {
    var results = StatsResource.query(function () {
        var cars = new Array();
        for (var i = 0; i < results.length; i++) {
            var result = results[i];
            var make = result.make;
            var count = result.count;
            cars.push(make + ":" + count);
        }
        $scope.cars = cars;
    });
});