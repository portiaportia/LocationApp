let map = null;
let marker = null;

window.showLocation = (latitude, longitude, address) => {
    const location = [latitude, longitude];

    if (map === null) {
        map = L.map("map").setView(location, 14);

        L.tileLayer(
            "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            {
                maxZoom: 19,
                attribution: "&copy; OpenStreetMap contributors"
            }
        ).addTo(map);

        marker = L.marker(location).addTo(map);
    } else {
        map.setView(location, 14);
        marker.setLatLng(location);
    }

    marker.bindPopup(address).openPopup();
};