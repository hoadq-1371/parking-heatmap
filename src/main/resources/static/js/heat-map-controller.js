app.factory('Citation', function ($resource) {
    return $resource('/citation');
});
app.controller('LayerHeatmapCtrl', function (NgMap, $resource, $scope, Citation) {
    var heatmap, vm = this;
    $scope.selection = {};
    var startTime = timestamp('March 01, 2016');
    var endTime = timestamp('April 01, 2016');
    NgMap.getMap().then(function (map) {

        vm.map = map;
        heatmap = vm.map.heatmapLayers.heatmap;
        heatmap.set('radius', 20)
        vm.loadRestEndPoint(startTime);
    });

    vm.toggleHeatmap = function (event) {
        heatmap.setMap(heatmap.getMap() ? null : vm.map);
    };

    vm.changeGradient = function () {
        var gradient = [
            'rgba(0, 255, 255, 0)',
            'rgba(0, 255, 255, 1)',
            'rgba(0, 191, 255, 1)',
            'rgba(0, 127, 255, 1)',
            'rgba(0, 63, 255, 1)',
            'rgba(0, 0, 255, 1)',
            'rgba(0, 0, 223, 1)',
            'rgba(0, 0, 191, 1)',
            'rgba(0, 0, 159, 1)',
            'rgba(0, 0, 127, 1)',
            'rgba(63, 0, 91, 1)',
            'rgba(127, 0, 63, 1)',
            'rgba(191, 0, 31, 1)',
            'rgba(255, 0, 0, 1)'
        ]
        heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
    }

    vm.changeRadius = function () {
        heatmap.set('radius', heatmap.get('radius') ? null : 20);
    }

    vm.changeOpacity = function () {
        heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
    }

    vm.loadRestEndPoint = function (time) {
        var carsArray = jsonToArray($scope.selection.cars)
        var ticketArray = jsonToArray($scope.selection.violations)
        var entries = Citation.query({timestamp: time, make: carsArray, ticket: ticketArray}, function () {
            citationData.length = 0;
            for (var i = 0; i < entries.length; i++) {
                var entry = entries[i];
                var lat = entry.location.latitude;
                var long = entry.location.longitude;
                if (lat && long) {
                    citationData.push(new google.maps.LatLng(lat, long));
                }
            }
            heatmap.setMap(vm.map);
            console.log("done loading")
        });
    }

    $scope.clear = function () {
        $scope.selection.violations = [];
        $scope.selection.cars = [];
        $scope.updateSelection()
    };

    $scope.updateSelection = function () {
        var time = document.getElementById('slider-date').noUiSlider.get();
        vm.loadRestEndPoint(time);
    };

    $scope.loadAllPoints = function () {
        vm.loadRestEndPoint(null);
        document.getElementById('event-time').innerHTML = "All data points";
    };

    function jsonToArray(json) {
        var array;
        if (json) {
            //remove json
            Object.keys(json).map(function (k) {
                if (json[k] == true) {
                    return k;
                } else {
                    delete json[k];
                }
            });
            //convert to array
            array = Object.keys(json).map(function (k) {
                if (json[k] == true) {
                    return k;
                }
            });
        }
        return array;
    }

    // TODO: Submit pull request to cdnjs
    // TODO: Refactor to use: https://github.com/vasyabigi/angular-nouislider
    //
    var dateSlider = document.getElementById('slider-date');
    var
        weekdays = [
            "Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday",
            "Saturday"
        ],
        months = [
            "January", "February", "March",
            "April", "May", "June", "July",
            "August", "September", "October",
            "November", "December"
        ];
    noUiSlider.create(dateSlider, {
        range: {
            min: startTime,
            max: endTime
        },

        step: 1 * 60 * 60 * 1000,
        start: [startTime],

        format: wNumb({
            decimals: 0
        })
    });

    var dateValues = [
        document.getElementById('event-time'),
    ];

    dateSlider.noUiSlider.on('update', function (values, handle) {
        var date = new Date(+values[handle]);
        dateValues[handle].innerHTML = formatDate(new Date(+values[handle])) + "+ 1HR";
    });

    dateSlider.noUiSlider.on('change', function (values, handle) {
        var date = new Date(+values[handle]);
        vm.loadRestEndPoint(date.getTime())
    });

    // Create a new date from a string, return as a timestamp.
    function timestamp(str) {
        return new Date(str).getTime();
    }

    function nth(d) {
        if (d > 3 && d < 21) return 'th';
        switch (d % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

// Create a string representation of the date.
    function formatDate(date) {
        date = normalizeTime(date);
        return weekdays[date.getDay()] + ", " +
            date.getDate() + nth(date.getDate()) + " " +
            months[date.getMonth()] + " " +
            date.getFullYear() + " Time: " + date.getHours() + ":00";
    }

    function normalizeTime(date) {
        var minutes = date.getMinutes()
        if (minutes == 59) {
            date.setMinutes(00)
        }
        return date;
    }

});