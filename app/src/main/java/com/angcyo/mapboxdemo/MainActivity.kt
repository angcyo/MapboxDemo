package com.angcyo.mapboxdemo

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Property
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.ImageSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.net.URL


class MainActivity : AppCompatActivity(), MapboxMap.OnMapClickListener {

    lateinit var mapboxMap: MapboxMap

    val shenzhen = LatLng(22.5456149800, 114.0544791000)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //必须在使用MapView之前调用
        Mapbox.getInstance(
            applicationContext,
            "pk.eyJ1Ijoid2F5dG8iLCJhIjoiY2p0ZmRzYmEwMWQxMDQwbXVicW1vaXRidCJ9.lkCxE4JJAsC31XNhkw8BYQ"
        )

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        checkPermissions()

        map_view.onCreate(savedInstanceState)

        val customView = LayoutInflater.from(this).inflate(
            R.layout.layout_marker_view_bubble, FrameLayout(this), false
        )
        //customView.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

//        map_view.getMapAsync { mapboxMap ->
//            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
//                Log.e("test", it.toString())
//
//                val markerViewManager = MarkerViewManager(map_view, mapboxMap)
//                val markerView = MarkerView(LatLng(22.547, 114.085947), customView)
//                markerViewManager.addMarker(markerView)
//
//            }
//            //0.22881869049588086 25.500000000000004
//            Log.e("test", "$mapboxMap ${mapboxMap.minZoomLevel} ${mapboxMap.maxZoomLevel}")
//
//            RMapbox.build(map_view).addMarker(LatLng(42.547, 114.085947), customView)
//        }

        RMapbox.init(map_view) { map, style ->
            mapboxMap = map

            map.isDebugActive = false

            RMapbox.build(map)

            markerTest(map, style)
//            RMapbox.build(map).addMarker(map_view, LatLng(22.547, 114.085947), customView)
//                .setMapDefaultLanguage(map_view, style)
//
//            val markerViewManager = MarkerViewManager(map_view, map)
//            val markerView = MarkerView(LatLng(22.547, 114.085947), customView)
//            markerViewManager.addMarker(markerView)

            L.e(style.url)

//            mapboxMap.addOnCameraMoveListener {
//                text_view.apply {
//                    text = "$text \n CameraMove"
//                }
//            }

            mapboxMap.locationComponent.apply {
                RMapbox.setOnLocationChangeListener(this) { location, fromLastLocation ->
                    L.e("位置改变:$location  缓存:$fromLastLocation")
                    text_view.apply {
                        text = "$text \n 位置改变:${location?.longitude} ${location?.latitude} 缓存:$fromLastLocation"
                    }
                }

                addOnLocationStaleListener {
                    text_view.apply {
                        text = "$text \n 发生改变:${lastKnownLocation?.longitude} ${lastKnownLocation?.latitude} :$it"
                    }
                }

                L.e("${this}")
                L.e("${this.locationEngine}")
                L.e("${this.compassEngine}")

                activateLocationComponent(applicationContext, style, true)
//                isLocationComponentEnabled = true
//                cameraMode = CameraMode.TRACKING
//                renderMode = RenderMode.COMPASS

                L.e("${this}")
                L.e("${this.locationEngine}")
                L.e("${this.compassEngine}")

                locationEngine?.requestLocationUpdates(
                    LocationEngineRequest.Builder(1000)
                        .setFastestInterval(1000)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build(), object : LocationEngineCallback<LocationEngineResult> {
                        override fun onSuccess(result: LocationEngineResult?) {
                            text_view.apply {
                                text =
                                    "$text \n 监听改变:${result?.lastLocation?.longitude} ${result?.lastLocation?.latitude} :${result?.locations?.size}"
                            }
                        }

                        override fun onFailure(exception: Exception) {
                            TODO("not implemented")
                            text_view.apply {
                                text =
                                    "$text \n 监听改变:${exception.message}"
                            }
                        }

                    }, Looper.getMainLooper()
                )
            }

            addLineLayer(style)
            addWms(style)
        }

//        web_view.webChromeClient = object : WebChromeClient() {
//
//        }

//        web_view.settings.javaScriptEnabled = true
//
//        web_view.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                view?.loadUrl(request!!.url.toString())
//                return true
//            }
//        }
//        web_view.loadUrl("http://116.7.249.35:8088/example/")
    }

    fun checkPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 999
        )
    }

    override fun onStart() {
        super.onStart()
        map_view.onStart()
    }

    override fun onResume() {
        super.onResume()
        map_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        map_view.onPause()
    }

    override fun onStop() {
        super.onStop()
        map_view.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_view.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_view.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map_view.onSaveInstanceState(outState)
    }

    fun markerTest(mapboxMap: MapboxMap, style: Style) {
        val layerList = style.layers

        mapboxMap.addMarker(MarkerOptions().position(shenzhen).title("Test Title"))

        val markerCoordinates = mutableListOf<Feature>()
        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(shenzhen.longitude, shenzhen.latitude)
            ).apply {
                properties()?.addProperty("my-marker-image", "marker1")
            }
        ) // Boston Common Park
        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(114.185947, 22.547)
            ).apply {
                properties()?.addProperty("my-marker-image", "marker2")
            }
        ) // Fenway Park
        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(114.285947, 22.547)
            )
        ) // The Paul Revere House

        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(114.075947, 22.547)
            )
        )

        style.addSource(
            GeoJsonSource(
                "marker-source",
                FeatureCollection.fromFeatures(markerCoordinates)
            )
        )

        // Add the marker image to map
        style.addImage(
            "my-marker-image", BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )
        style.addImage(
            "marker1", BitmapFactory.decodeResource(
                this.resources, R.drawable.red_marker
            )
        )
        style.addImage(
            "marker2", BitmapFactory.decodeResource(
                this.resources, R.drawable.pink_dot
            )
        )
        style.addImage(
            "logo", BitmapFactory.decodeResource(
                this.resources, R.drawable.ic_launcher
            )
        )

//        style.addImages(
//            hashMapOf(
//                Pair(
//                    "marker1", BitmapFactory.decodeResource(
//                        this.resources, R.drawable.red_marker
//                    )
//                ),
//                Pair(
//                    "marker2", BitmapFactory.decodeResource(
//                        this.resources, R.drawable.pink_dot
//                    )
//                )
//            )
//        )

// Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
// middle of the icon being fixed to the coordinate point.
        style.addLayer(
            SymbolLayer("marker-layer", "marker-source")
                .withProperties(
                    iconImage("{my-marker-image}"),
                    //textField("88"),
                    textSize(17f),
                    textOffset(arrayOf(0f, -2.5f)),
                    textColor(Color.BLUE),
                    textAllowOverlap(true),
                    textIgnorePlacement(true),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
//                .withProperties(
//                    PropertyFactory.iconImage("my-marker-image"),
//                    PropertyFactory.iconOffset(arrayOf(0f, -9f)),
//                    PropertyFactory.iconSize(0.8f),
//                    PropertyFactory.iconAllowOverlap(true),
//                    PropertyFactory.iconIgnorePlacement(true),
//
//                    PropertyFactory.textField("Test"),
//                    PropertyFactory.textSize(20f),
//                    PropertyFactory.textColor(Color.RED),
//                    PropertyFactory.textAllowOverlap(true),
//                    PropertyFactory.textIgnorePlacement(true)
//
//                )
        )

// Add the selected marker source and layer
        style.addSource(GeoJsonSource("selected-marker"))

// Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
// middle of the icon being fixed to the coordinate point.
        style.addLayer(
            SymbolLayer("selected-marker-layer", "selected-marker")
                .withProperties(
                    PropertyFactory.iconImage("my-marker-image"),
                    PropertyFactory.iconOffset(arrayOf(0f, -109f))
                )
        )

        mapboxMap.addOnMapClickListener(this)
    }

    private fun selectMarker(iconLayer: SymbolLayer) {
        val markerAnimator = ValueAnimator()
        markerAnimator.setObjectValues(1f, 2f)
        markerAnimator.duration = 300
        markerAnimator.addUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator.start()
        markerSelected = true
    }

    private fun deselectMarker(iconLayer: SymbolLayer) {
        val markerAnimator = ValueAnimator()
        markerAnimator.setObjectValues(2f, 1f)
        markerAnimator.duration = 300
        markerAnimator.addUpdateListener { animator ->
            iconLayer.setProperties(
                PropertyFactory.iconSize(animator.animatedValue as Float)
            )
        }
        markerAnimator.start()
        markerSelected = false
    }

    var markerSelected = false

    @SuppressLint("MissingPermission")
    override fun onMapClick(point: LatLng): Boolean {
        val location = mapboxMap.locationComponent.lastKnownLocation
        L.e("LastLocation-> ${location}")

        text_view.apply {
            text = "$text \n 获取位置:${location?.longitude} ${location?.latitude}"
        }

        // Convert LatLng coordinates to screen pixel and only query the rendered features.
        val pixel = mapboxMap.projection.toScreenLocation(point)
        val features = mapboxMap.queryRenderedFeatures(pixel)

        // Get the first feature within the list if one exist
        if (features.size > 0) {
            val feature = features[0]

            // Ensure the feature has properties defined
            if (feature.properties() != null) {
                for ((key, value) in feature.properties()!!.entrySet()) {
                    // Log all the properties
                    L.e(String.format("%s = %s", key, value))
                }
            }
        }


        val style = mapboxMap.style
        if (style != null) {
            val layerList = style.layers

            val selectedMarkerSymbolLayer = style.getLayer("selected-marker-layer") as SymbolLayer

            val pixel = mapboxMap.projection.toScreenLocation(point)
            val features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer")
            val selectedFeature = mapboxMap.queryRenderedFeatures(pixel, "selected-marker-layer")

            if (selectedFeature.size > 0 && markerSelected) {
                return false
            }

            if (features.isEmpty()) {
                if (markerSelected) {
                    deselectMarker(selectedMarkerSymbolLayer)
                }
                return false
            }

            val source: GeoJsonSource? = style.getSourceAs("selected-marker")
            if (source != null) {
                source.setGeoJson(
                    FeatureCollection.fromFeatures(
                        arrayOf(features[0])
                    )
                )
            }

            if (markerSelected) {
                deselectMarker(selectedMarkerSymbolLayer)
            }
            if (features.size > 0) {
                selectMarker(selectedMarkerSymbolLayer)
            }
        }
        return true
    }


    fun addLineLayer(style: Style) {
        style.addSource(
            GeoJsonSource(
                "line-source",
                FeatureCollection.fromFeature(
                    Feature.fromGeometry(
                        LineString.fromLngLats(
                            mutableListOf<Point>().apply {
                                for (i in 0..10) {
                                    add(Point.fromLngLat(shenzhen.longitude + 0.1 * i, shenzhen.latitude + 0.1 * i))
                                }
                                for (i in 10..20) {
                                    add(Point.fromLngLat(shenzhen.longitude + 0.1 * i, shenzhen.latitude - 0.01 * i))
                                }
                            }
                        )
                    )
                )
            )
        )
        style.addLayer(
            LineLayer("line-layer", "line-source").withProperties(
                PropertyFactory.lineColor(Color.YELLOW),
                PropertyFactory.lineWidth(4f),
                PropertyFactory.lineDasharray(arrayOf(5f, 10f)),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND)
            )
        )

        style.addLayer(
            CircleLayer("circle-layer", "line-source").withProperties(
                PropertyFactory.circleStrokeColor(Color.RED),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleColor(Color.BLUE)
            )
        )
    }

    fun addWms(style: Style) {
//        val url =
//            "http://116.7.249.35:8088/geoserver/saas/wfs?service=WFS&version=1.0.0&request=GetFeature&typeName=saas%3Ag_assetpoint&maxFeatures=1000&outputFormat=application%2Fjson"
//
//
//        style.addSource(GeoJsonSource("wms_source", URL(url)))
//
//        style.addLayer(
//            SymbolLayer("wms_layer", "wms_source").withProperties(
//                PropertyFactory.iconImage("logo")
//            )
//        )
//


        val url2 =
            "http://116.7.249.37:6080/arcgis/services/Valves/MapServer/WMSServer?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&LAYERS=3&exceptions=application%2Fvnd.ogc.se_inimage&CRS=EPSG%3A3857&STYLES=&WIDTH=1904&HEIGHT=800&BBOX=8042398.368053104%2C621280.1659019124%2C17356708.88677154%2C4534856.014102937"
        //val url2 = "https://geodata.state.nj.us/imagerywms/Natural2015?bbox={bbox-epsg-3857}&format=image/png&service=WMS&version=1.1.1&request=GetMap&srs=EPSG:3857&width=256&height=256&layers=Natural2015"

//        style.addSource(
//            RasterSource(
//                "web-map-source",
//                TileSet("tileset", url2), 10
//            )
//        )
//
////        style.addLayerBelow(
////            RasterLayer("web-map-layer", "web-map-source"), "aeroway-taxiway"
////        );
//        style.addLayer(
//            RasterLayer("web-map-layer", "web-map-source")
//        )

        style.addSource(
            ImageSource(
                "image_source",
                LatLngQuad(
                    LatLng(shenzhen.latitude - 0.1, shenzhen.longitude - 0.1),
                    LatLng(shenzhen.latitude - 0.1, shenzhen.longitude + 0.5),
                    LatLng(shenzhen.latitude - 0.2, shenzhen.longitude + 0.7),
                    LatLng(shenzhen.latitude - 0.3, shenzhen.longitude + 0.8)
                ), URL(url2)
            )
        )

// Add layer
        style.addLayer(RasterLayer("raster_image_layer", "image_source"))
    }

}
