package com.angcyo.mapboxdemo

import android.app.Application

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/04/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RMapbox.token(this)
    }
}