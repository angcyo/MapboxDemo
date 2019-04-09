package com.angcyo.mapboxdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.angcyo.mapboxdemo.RMapbox.TOKEN
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.china.constants.ChinaStyle
import com.mapbox.mapboxsdk.plugins.china.maps.ChinaMapView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class NewActivity : AppCompatActivity() {

    lateinit var map_view: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        map_view = MapView(this)
        setContentView(map_view)

        map_view.onCreate(savedInstanceState)

//        map_view.getMapAsync {
//            //it.setStyle(ChinaStyle.MAPBOX_STREETS_CHINESE)
//            it.setStyle(Style.MAPBOX_STREETS)
//        }


        RMapbox.init(map_view) { map, style ->

        }
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
}