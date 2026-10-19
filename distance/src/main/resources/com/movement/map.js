var map = null;
var markers = [];
var routeLine = null;

function createMap() {
    if (map !== null) {
        return;
    }

    map = L.map("map").setView([32.7765, -79.9311], 11);

    L.tileLayer(
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
        {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap contributors"
        }
    ).addTo(map);
}

window.showLocations = function(locations) {
    createMap();

    markers.forEach(function(marker) {
        marker.remove();
    });

    markers = [];

    var bounds = L.latLngBounds();

    locations.forEach(function(location) {
        var position = [location.latitude, location.longitude];

        var marker = L.marker(position)
            .addTo(map)
            .bindPopup(location.address);

        markers.push(marker);
        bounds.extend(position);
    });

    if (locations.length === 1) {
        map.setView(
            [locations[0].latitude, locations[0].longitude],
            14
        );
    } else {
        map.fitBounds(bounds, { padding: [40, 40] });
    }
};

window.showRoute = function(routePoints) {
    if (routeLine !== null) {
        routeLine.remove();
    }

    routeLine = L.polyline(
        routePoints,
        {
            color: "blue",
            weight: 5
        }
    ).addTo(map);

    map.fitBounds(
        routeLine.getBounds(),
        { padding: [40, 40] }
    );
};