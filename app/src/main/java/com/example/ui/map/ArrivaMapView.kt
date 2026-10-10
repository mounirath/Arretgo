package com.example.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.FavoritePlace
import com.example.model.LocationPoint
import com.example.model.MapStyle
import com.example.model.UserLocation

interface MapBridgeListener {
    fun onMapClicked(lat: Double, lng: Double)
    fun onMapReady()
    fun onFavoriteSelected(id: Long)
}

class MapJsBridge(private val listener: MapBridgeListener) {
    @JavascriptInterface
    fun onMapClicked(lat: Double, lng: Double) {
        listener.onMapClicked(lat, lng)
    }

    @JavascriptInterface
    fun onMapLoaded() {
        listener.onMapReady()
    }

    @JavascriptInterface
    fun onFavoriteSelected(id: Long) {
        listener.onFavoriteSelected(id)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArrivaMapView(
    userLocation: UserLocation,
    destination: LocationPoint?,
    alertRadiusMeters: Int,
    mapStyle: MapStyle,
    favorites: List<FavoritePlace> = emptyList(),
    onSelectFavorite: (FavoritePlace) -> Unit = {},
    onMapClick: (Double, Double) -> Unit,
    centerUserTrigger: Long = 0L,
    centerDestTrigger: Long = 0L,
    zoomInTrigger: Long = 0L,
    zoomOutTrigger: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bridgeListener = remember(favorites) {
        object : MapBridgeListener {
            override fun onMapClicked(lat: Double, lng: Double) {
                onMapClick(lat, lng)
            }
            override fun onMapReady() {}
            override fun onFavoriteSelected(id: Long) {
                val found = favorites.find { it.id == id }
                if (found != null) {
                    onSelectFavorite(found)
                }
            }
        }
    }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // Set valid identifying user agent for OpenStreetMap tile servers
            settings.userAgentString = "ArrevaGPS/1.2 (Android; Mobile OSM Map Client; contact: support@arreva.app)"

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    android.util.Log.d("ArrivaMap", "JS: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript("""
                        if (typeof initLeafletMap === 'function') { initLeafletMap(); }
                        if (window.map) { window.map.invalidateSize(); }
                    """.trimIndent(), null)
                }

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    android.util.Log.e("ArrivaMap", "WebView Error: ${error?.description}")
                }
            }

            addJavascriptInterface(MapJsBridge(bridgeListener), "Android")
            // Load using file:///android_asset/ for instant 0ms offline-ready Leaflet bundle
            loadDataWithBaseURL("file:///android_asset/", generateMapHtml(mapStyle.id, userLocation.latitude, userLocation.longitude), "text/html", "UTF-8", null)
        }
    }

    // Auto-invalidate map size when mounted to ensure full screen tile rendering
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        webView.evaluateJavascript("if (window.map) { window.map.invalidateSize(); }", null)
        kotlinx.coroutines.delay(500)
        webView.evaluateJavascript("if (window.map) { window.map.invalidateSize(); }", null)
    }

    // Update User Location Marker in Map
    LaunchedEffect(userLocation.latitude, userLocation.longitude, userLocation.accuracyMeters) {
        val script = "if (window.updateUser) { window.updateUser(${userLocation.latitude}, ${userLocation.longitude}, ${userLocation.accuracyMeters}); }"
        webView.evaluateJavascript(script, null)
    }

    // Update Saved Favorite Stops in Map
    LaunchedEffect(favorites) {
        val favJsonArray = favorites.joinToString(separator = ",", prefix = "[", postfix = "]") { fav ->
            """{"id":${fav.id},"name":"${fav.name.replace("\"", "\\\"")}","address":"${fav.address.replace("\"", "\\\"")}","lat":${fav.latitude},"lng":${fav.longitude},"tag":"${fav.tag.replace("\"", "\\\"")}","radius":${fav.defaultRadiusMeters}}"""
        }
        val script = "if (window.updateFavorites) { window.updateFavorites($favJsonArray); }"
        webView.evaluateJavascript(script, null)
    }

    // Update Destination & Alert Radius Circle in Map
    LaunchedEffect(destination?.latitude, destination?.longitude, alertRadiusMeters) {
        val dest = destination
        if (dest != null) {
            val script = "if (window.setDestination) { window.setDestination(${dest.latitude}, ${dest.longitude}, '${dest.name.replace("'", "\\'")}', $alertRadiusMeters); } if (window.centerOnDest) { window.centerOnDest(); }"
            webView.evaluateJavascript(script, null)
        } else {
            val script = "if (window.clearDestination) { window.clearDestination(); }"
            webView.evaluateJavascript(script, null)
        }
    }

    // Update Map Tile Layer
    LaunchedEffect(mapStyle) {
        val script = "if (window.setTileLayer) { window.setTileLayer('${mapStyle.id}'); }"
        webView.evaluateJavascript(script, null)
    }

    // Action Triggers
    LaunchedEffect(centerUserTrigger) {
        if (centerUserTrigger > 0L) {
            webView.evaluateJavascript("if (window.centerOnUser) { window.centerOnUser(${userLocation.latitude}, ${userLocation.longitude}); }", null)
        }
    }

    LaunchedEffect(centerDestTrigger) {
        if (centerDestTrigger > 0L) {
            webView.evaluateJavascript("if (window.centerOnDest) { window.centerOnDest(); }", null)
        }
    }

    LaunchedEffect(zoomInTrigger) {
        if (zoomInTrigger > 0L) {
            webView.evaluateJavascript("if (window.zoomIn) { window.zoomIn(); }", null)
        }
    }

    LaunchedEffect(zoomOutTrigger) {
        if (zoomOutTrigger > 0L) {
            webView.evaluateJavascript("if (window.zoomOut) { window.zoomOut(); }", null)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

private fun generateMapHtml(initialStyle: String, initialLat: Double, initialLng: Double): String {
    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <title>Google Maps</title>
    <!-- Local Android Assets Leaflet Bundle with instant fallback -->
    <link rel="stylesheet" href="leaflet/leaflet.css" />
    <link rel="stylesheet" href="file:///android_asset/leaflet/leaflet.css" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="leaflet/leaflet.js"></script>
    <script src="file:///android_asset/leaflet/leaflet.js"></script>
    <script>
        if (typeof L === 'undefined') {
            document.write('<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"><\/script>');
        }
    </script>
    <style>
        * { margin:0; padding:0; box-sizing:border-box; -webkit-tap-highlight-color: transparent; }
        html, body {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #e8ecf1;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        #map {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
            background: #e8ecf1;
        }
        .leaflet-control-attribution, .leaflet-control-zoom { display:none !important; }
        
        /* User Radar Blue Marker */
        .user-pulse-container {
            position: relative;
            width: 48px;
            height: 48px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .user-center-dot {
            width: 18px;
            height: 18px;
            background: #1a73e8;
            border: 3px solid #ffffff;
            border-radius: 50%;
            box-shadow: 0 0 12px rgba(26, 115, 232, 0.85);
            z-index: 2;
        }
        .user-radar-ring {
            position: absolute;
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: rgba(26, 115, 232, 0.30);
            animation: radarPulse 2s infinite ease-out;
            z-index: 1;
        }
        @keyframes radarPulse {
            0% { transform: scale(0.5); opacity: 1; }
            100% { transform: scale(1.6); opacity: 0; }
        }

        /* Destination Red Pin */
        .dest-pin-container {
            width: 44px;
            height: 52px;
            display: flex;
            flex-direction: column;
            align-items: center;
            animation: bouncePin 2s infinite ease-in-out;
            transform-origin: bottom center;
            filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.35));
        }
        @keyframes bouncePin {
            0%, 100% { transform: translateY(0); }
            50% { transform: translateY(-7px); }
        }

        /* Saved Favorite Gold Star Pin */
        .fav-pin-pulse {
            position: relative;
            width: 38px;
            height: 46px;
            display: flex;
            flex-direction: column;
            align-items: center;
            filter: drop-shadow(0 6px 12px rgba(245, 158, 11, 0.5));
            cursor: pointer;
            transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        .fav-pin-pulse:hover, .fav-pin-pulse:active {
            transform: scale(1.2) translateY(-4px);
        }
        .fav-pin-star-bubble {
            width: 32px;
            height: 32px;
            background: linear-gradient(135deg, #FDE68A 0%, #F59E0B 60%, #D97706 100%);
            border: 2px solid #FFFFFF;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #FFFFFF;
            font-size: 16px;
            font-weight: bold;
            box-shadow: inset 0 2px 4px rgba(255, 255, 255, 0.8), 0 3px 6px rgba(0, 0, 0, 0.2);
        }
        .fav-pin-stem {
            width: 4px;
            height: 10px;
            background: #D97706;
            border-radius: 2px;
            margin-top: -2px;
        }

        /* Frutiger Aero Callout Popup */
        .leaflet-popup-content-wrapper {
            background: rgba(255, 255, 255, 0.96) !important;
            backdrop-filter: blur(12px) !important;
            border-radius: 20px !important;
            border: 1.5px solid rgba(255, 255, 255, 0.95) !important;
            box-shadow: 0 16px 32px rgba(2, 132, 199, 0.3), 0 4px 10px rgba(0, 0, 0, 0.1) !important;
            padding: 0 !important;
            overflow: hidden !important;
        }
        .leaflet-popup-content {
            margin: 14px 16px !important;
            line-height: 1.4 !important;
        }
        .fav-callout-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 6px;
        }
        .fav-callout-tag {
            background: #FEF3C7;
            color: #92400E;
            font-size: 11px;
            font-weight: 700;
            padding: 3px 8px;
            border-radius: 12px;
            border: 1px solid #FCD34D;
        }
        .fav-callout-name {
            color: #0A2540;
            font-size: 15px;
            font-weight: 800;
            margin-bottom: 2px;
        }
        .fav-callout-addr {
            color: #475569;
            font-size: 11px;
            margin-bottom: 8px;
        }
        .fav-callout-radius {
            color: #15803D;
            font-size: 11px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        .fav-callout-btn {
            width: 100%;
            padding: 8px 12px;
            background: linear-gradient(180deg, #38BDF8 0%, #0284C7 60%, #10B981 100%);
            border: 1px solid #FFFFFF;
            border-radius: 12px;
            color: #FFFFFF;
            font-size: 12px;
            font-weight: 700;
            box-shadow: 0 4px 10px rgba(2, 132, 199, 0.35);
            cursor: pointer;
            text-align: center;
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var map = null;

        function initLeafletMap() {
            if (map) return;
            var lat = $initialLat || 48.8566;
            var lng = $initialLng || 2.3522;

            map = L.map('map', {
                center: [lat, lng],
                zoom: 14,
                zoomControl: false,
                attributionControl: false
            });

            // OpenStreetMap Standard - fast reliable tile servers with automatic mirror recovery
            var osmLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '© OpenStreetMap contributors'
            });

            // Auto-recovery fallback for any blocked or failed OSM tile
            osmLayer.on('tileerror', function(error, tile) {
                if (tile && !tile._fallbackCount) {
                    tile._fallbackCount = 1;
                    var z = error.coords.z;
                    var x = error.coords.x;
                    var y = error.coords.y;
                    tile.src = 'https://a.tile.openstreetmap.fr/osmfr/' + z + '/' + x + '/' + y + '.png';
                } else if (tile && tile._fallbackCount === 1) {
                    tile._fallbackCount = 2;
                    var z = error.coords.z;
                    var x = error.coords.x;
                    var y = error.coords.y;
                    tile.src = 'https://a.basemaps.cartocdn.com/rastertiles/voyager/' + z + '/' + x + '/' + y + '.png';
                }
            });

            // Carto Voyager (Clean, vivid Google Maps styling, 100% accessible worldwide)
            var voyagerLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                maxZoom: 19,
                subdomains: ['a', 'b', 'c', 'd']
            });

            // Esri Satellite (High resolution real satellite imagery)
            var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                maxZoom: 19
            });

            // OpenTopoMap (Topographic Terrain)
            var terrainLayer = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                maxZoom: 17,
                subdomains: ['a', 'b', 'c']
            });

            // Carto Dark
            var darkLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                maxZoom: 19,
                subdomains: ['a', 'b', 'c', 'd']
            });

            var tileLayers = {
                osm: osmLayer,
                google: voyagerLayer,
                satellite: satelliteLayer,
                terrain: terrainLayer,
                dark: darkLayer
            };

            var currentStyleKey = '$initialStyle' || 'osm';
            var currentLayer = tileLayers[currentStyleKey] || osmLayer;
            currentLayer.addTo(map);

            window.setTileLayer = function(styleId) {
                if (currentLayer) map.removeLayer(currentLayer);
                currentLayer = tileLayers[styleId] || osmLayer;
                currentLayer.addTo(map);
            };

            // User Location Marker
            var userIcon = L.divIcon({
                className: 'user-icon-leaflet',
                html: '<div class="user-pulse-container"><div class="user-radar-ring"></div><div class="user-center-dot"></div></div>',
                iconSize: [48, 48],
                iconAnchor: [24, 24]
            });
            var userMarker = L.marker([lat, lng], { icon: userIcon }).addTo(map);

            window.updateUser = function(newLat, newLng, accuracy) {
                if (!userMarker) {
                    userMarker = L.marker([newLat, newLng], { icon: userIcon }).addTo(map);
                } else {
                    userMarker.setLatLng([newLat, newLng]);
                }
                updateRoute();
            };

            // Destination Marker
            var destPinSvg = '<svg width="40" height="48" viewBox="0 0 40 48" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M20 0C8.954 0 0 8.954 0 20C0 35 20 48 20 48C20 48 40 35 40 20C40 8.954 31.046 0 20 0Z" fill="#EA4335"/>' +
                '<circle cx="20" cy="18" r="8" fill="white"/>' +
                '<circle cx="20" cy="18" r="4" fill="#B31412"/>' +
                '</svg>';

            var destIcon = L.divIcon({
                className: 'dest-icon-leaflet',
                html: '<div class="dest-pin-container">' + destPinSvg + '</div>',
                iconSize: [40, 48],
                iconAnchor: [20, 46]
            });

            var destMarker = null;
            var geofenceCircle = null;
            var routePolyline = null;

            window.setDestination = function(destLat, destLng, name, radius) {
                if (!destMarker) {
                    destMarker = L.marker([destLat, destLng], { icon: destIcon }).addTo(map);
                } else {
                    destMarker.setLatLng([destLat, destLng]);
                }

                if (!geofenceCircle) {
                    geofenceCircle = L.circle([destLat, destLng], {
                        radius: radius,
                        color: '#00E5FF',
                        fillColor: '#00E5FF',
                        fillOpacity: 0.20,
                        weight: 2,
                        dashArray: '5, 8'
                    }).addTo(map);
                } else {
                    geofenceCircle.setLatLng([destLat, destLng]);
                    geofenceCircle.setRadius(radius);
                }

                fitBoth();
                updateRoute();
            };

            window.clearDestination = function() {
                if (destMarker) { map.removeLayer(destMarker); destMarker = null; }
                if (geofenceCircle) { map.removeLayer(geofenceCircle); geofenceCircle = null; }
                if (routePolyline) { map.removeLayer(routePolyline); routePolyline = null; }
            };

            // Saved Favorite Stops Layer & Interactive Callout
            var favoritesLayer = L.layerGroup().addTo(map);

            window.updateFavorites = function(favoritesList) {
                favoritesLayer.clearLayers();
                if (!favoritesList || !favoritesList.length) return;

                favoritesList.forEach(function(fav) {
                    var favIcon = L.divIcon({
                        className: 'fav-marker-leaflet',
                        html: '<div class="fav-pin-pulse"><div class="fav-pin-star-bubble">★</div><div class="fav-pin-stem"></div></div>',
                        iconSize: [38, 46],
                        iconAnchor: [19, 44],
                        popupAnchor: [0, -44]
                    });

                    var marker = L.marker([fav.lat, fav.lng], { icon: favIcon });

                    var popupHtml = '<div class="fav-callout-container">' +
                        '<div class="fav-callout-header">' +
                            '<span class="fav-callout-tag">★ ' + (fav.tag || 'Favori') + '</span>' +
                        '</div>' +
                        '<div class="fav-callout-name">' + fav.name + '</div>' +
                        (fav.address ? '<div class="fav-callout-addr">' + fav.address + '</div>' : '') +
                        '<div class="fav-callout-radius">🔔 Réveil à ' + fav.radius + ' m</div>' +
                        '<button class="fav-callout-btn" onclick="window.Android.onFavoriteSelected(' + fav.id + ')">' +
                            '🎯 Définir comme destination' +
                        '</button>' +
                    '</div>';

                    marker.bindPopup(popupHtml, {
                        maxWidth: 240,
                        className: 'frutiger-aero-popup'
                    });

                    favoritesLayer.addLayer(marker);
                });
            };

            function updateRoute() {
                if (userMarker && destMarker) {
                    var userLatLng = userMarker.getLatLng();
                    var destLatLng = destMarker.getLatLng();
                    var points = [userLatLng, destLatLng];
                    if (!routePolyline) {
                        routePolyline = L.polyline(points, {
                            color: '#1a73e8',
                            weight: 4,
                            dashArray: '6, 8',
                            opacity: 0.9,
                            lineCap: 'round'
                        }).addTo(map);
                    } else {
                        routePolyline.setLatLngs(points);
                    }
                }
            }

            function fitBoth() {
                if (userMarker && destMarker && geofenceCircle) {
                    var group = new L.featureGroup([userMarker, destMarker, geofenceCircle]);
                    map.fitBounds(group.getBounds().pad(0.3));
                } else if (destMarker) {
                    map.setView(destMarker.getLatLng(), 15);
                }
            }

            // Map Control Bridge APIs
            window.centerOnUser = function(optLat, optLng) {
                if (optLat !== undefined && optLng !== undefined) {
                    map.flyTo([optLat, optLng], 16, { duration: 1.0 });
                } else if (userMarker) {
                    map.flyTo(userMarker.getLatLng(), 16, { duration: 1.0 });
                }
            };

            window.centerOnDest = function() {
                if (destMarker) {
                    map.flyTo(destMarker.getLatLng(), 16, { duration: 1.2 });
                }
            };

            window.zoomIn = function() {
                map.zoomIn();
            };

            window.zoomOut = function() {
                map.zoomOut();
            };

            map.on('click', function(e) {
                if (window.Android && window.Android.onMapClicked) {
                    window.Android.onMapClicked(e.latlng.lat, e.latlng.lng);
                }
            });

            function triggerResize() {
                if (map) {
                    map.invalidateSize();
                }
            }
            window.addEventListener('resize', triggerResize);
            setTimeout(triggerResize, 50);
            setTimeout(triggerResize, 200);
            setTimeout(triggerResize, 800);

            if (window.Android && window.Android.onMapLoaded) {
                window.Android.onMapLoaded();
            }
        }

        // Initialize immediately
        if (typeof L !== 'undefined') {
            initLeafletMap();
        } else {
            window.addEventListener('DOMContentLoaded', function() {
                if (typeof L !== 'undefined') initLeafletMap();
            });
        }
    </script>
</body>
</html>
    """.trimIndent()
}
