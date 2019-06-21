app.factory('CarMakeResource', function ($resource) {
    return $resource('/citation/car/manufacturer');
});
app.factory('TicketResource', function ($resource) {
    return $resource('/citation/violation');
});
app.controller('FilterCtrl', function (NgMap, $resource, $scope, CarMakeResource, TicketResource) {
    var cars = CarMakeResource.query(function () {
        var carArray = new Array();
        for (var i = 0; i < cars.length; i++) {
            carArray.push({"name": cars[i], "id": cars[i]});
        }
        $scope.cars = carArray;
    });

    var violations = TicketResource.query(function () {
       var violationArray = new Array();
        for (var i = 0; i < violations.length; i++) {
            violationArray.push({"name": violations[i].description, "id": violations[i].id});
        }
        $scope.violations = violationArray;
    });
});