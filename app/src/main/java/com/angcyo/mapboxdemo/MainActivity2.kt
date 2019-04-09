package com.angcyo.mapboxdemo

import android.Manifest
import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.content_main2.*


class MainActivity2 : AppCompatActivity(), MapboxMap.OnMapClickListener {

    lateinit var mapboxMap: MapboxMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //必须在使用MapView之前调用
        Mapbox.getInstance(
            applicationContext,
            "pk.eyJ1Ijoid2F5dG8iLCJhIjoiY2p0ZmRzYmEwMWQxMDQwbXVicW1vaXRidCJ9.lkCxE4JJAsC31XNhkw8BYQ"
        )

        setContentView(R.layout.activity_main2)
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
        }

//        web_view.webChromeClient = object : WebChromeClient() {
//
//        }

        web_view.settings.javaScriptEnabled = true

        web_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request!!.url.toString())
                return true
            }
        }
        web_view.loadUrl("http://116.7.249.35:8088/example/")
    }

    fun checkPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
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
        val markerCoordinates = mutableListOf<Feature>()
        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(-71.065634, 42.354950)
            )
        ) // Boston Common Park
        markerCoordinates.add(
            Feature.fromGeometry(
                Point.fromLngLat(114.185947, 22.547)
            )
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

// Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
// middle of the icon being fixed to the coordinate point.
        style.addLayer(
            SymbolLayer("marker-layer", "marker-source")
                .withProperties(
                    PropertyFactory.iconImage("my-marker-image"),
                    PropertyFactory.iconOffset(arrayOf(0f, -9f)),
                    PropertyFactory.iconSize(0.3f)
                )
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

    override fun onMapClick(point: LatLng): Boolean {
        val style = mapboxMap.style
        if (style != null) {
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
}
