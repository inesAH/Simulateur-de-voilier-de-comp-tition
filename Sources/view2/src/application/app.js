/*
script de l'application
 */

var brest = [ 48.381171, -4.48697 ];

function initDemoMap() {

	var Hydda_Base = L
			.tileLayer(
					'https://{s}.tile.openstreetmap.se/hydda/base/{z}/{x}/{y}.png',
					{
						minZoom : 6 / 4,
						maxZoom : 12,
						attribution : 'Tiles courtesy of <a href="http://openstreetmap.se/" target="_blank">OpenStreetMap Sweden</a> &mdash; Map data &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
					});

	var baseLayers = {
		"Satellite" : Hydda_Base,
	};

	var map = L.map('mapid', {
		layers : [ Hydda_Base ]
	});

	var layerControl = L.control.layers(baseLayers);
	layerControl.addTo(map);
	map.setView(brest, 10);

	return {
		map : map,
		layerControl : layerControl
	};
}

// demo map
var mapStuff = initDemoMap();
var map = mapStuff.map;
var layerControl = mapStuff.layerControl;
var handleError = function(err) {
	console.log('handleError...');
	console.log(err);
};

var data;
var popup = L.popup();
var pointsArray;
var markerGroup;
var trajet;

function showOnMap(input_geo_json, freq, valSlider) {
	pointsArray = new Array()
	data = input_geo_json;
	markerGroup = new L.layerGroup();
	var dataLenth = (data.length * valSlider) / 100;

	for (var i = 0; i < dataLenth; i += freq) {
		var marker = new L.circle([ data[i].lat, data[i].lng ], {
			color : 'red',
			fillColor : '#f03',
			fillOpacity : 0.5,
			radius : 200
		}).addTo(markerGroup);
		marker.on('mouseover', sendData);
		marker.on('click', showPopUp);
		pointsArray.push([ data[i].lat, data[i].lng ]);
	}

	trajet = new L.Polyline(pointsArray);
	map.addLayer(trajet);
	map.fitBounds(trajet.getBounds());
	map.addLayer(markerGroup);

}

function showPopUp(e) {
	popup.setLatLng(e.latlng).setContent("" + e.latlng.toString()).openOn(map);

}

function sendData(e) {
	for (var i = 0; i < data.length; i++) {
		if (e.latlng.equals(new L.LatLng(data[i].lat, data[i].lng))) {
			window.dataToSend.receveData(data[i].BS, data[i].TWS, data[i].TWA,
					data[i].RDA, data[i].hdg, data[i].TWD, data[i].AWA,
					data[i].AWS);
		}
	}
}

function clearMap() {
	map.removeLayer(markerGroup);
	map.removeLayer(trajet);
}

WindJSLeaflet.init({
	localMode : true,
	map : map,
	layerControl : layerControl,
	useNearest : false,
	timeISO : null,
	nearestDaysLimit : 7,
	displayValues : true,
	displayOptions : {
		displayPosition : 'bottomleft',
		displayEmptyString : 'No wind data'
	},
	overlayName : 'wind',

	errorCallback : handleError
});