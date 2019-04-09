package com.angcyo.mapboxdemo;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.localization.MapLocale;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
public class RMapbox {

    public static void init(@NonNull MapView mapView, @Nullable OnMapReady onMapReady) {
        //ChinaStyle.MAPBOX_STREETS_CHINESE
        init(mapView, Style.MAPBOX_STREETS, onMapReady);
    }

    public static void init(@NonNull final MapView mapView, @NonNull final String style, @Nullable final OnMapReady onMapReady) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(style, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        build(mapboxMap).setMapDefaultLanguage(mapView, style);
                        if (onMapReady != null) {
                            onMapReady.onMapReady(mapboxMap, style);
                        }
                    }
                });
            }
        });
    }

    /**
     * 监听位置改变
     */
    public static void setOnLocationChangeListener(@NonNull LocationComponent locationComponent,
                                                   @NonNull OnLocationChangeListener listener) {

        try {
            Class<? extends LocationComponent> locationComponentClass = locationComponent.getClass();
            Field lastLocationEngineListenerField = locationComponentClass
                    .getDeclaredField("lastLocationEngineListener");

            Field currentLocationEngineListenerField = locationComponentClass
                    .getDeclaredField("currentLocationEngineListener");

            currentLocationEngineListenerField.setAccessible(true);
            lastLocationEngineListenerField.setAccessible(true);

            currentLocationEngineListenerField.set(locationComponent,
                    new CurrentLocationEngineCallback(locationComponent, listener));

            lastLocationEngineListenerField.set(locationComponent,
                    new LastLocationEngineCallback(locationComponent, listener));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void updateLocation(@NonNull LocationComponent locationComponent,
                                       @Nullable final Location location,
                                       boolean fromLastLocation) {
        try {
            Method updateLocation = locationComponent.getClass()
                    .getDeclaredMethod("updateLocation", Location.class, boolean.class);
            updateLocation.setAccessible(true);
            updateLocation.invoke(locationComponent, location, fromLastLocation);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public interface OnLocationChangeListener {
        void onLocationChange(@Nullable final Location location, boolean fromLastLocation);
    }

    public static Builder build(@NonNull MapboxMap mapboxMap) {
        return new Builder(mapboxMap);
    }

    static class Builder {
        MapboxMap mapboxMap;

        public Builder(@NonNull MapboxMap mapboxMap) {
            this.mapboxMap = mapboxMap;
        }

        /**
         * @see com.mapbox.mapboxsdk.maps.OnMapReadyCallback#onMapReady(MapboxMap)
         * @see Style.OnStyleLoaded#onStyleLoaded(Style)
         */
        public Builder setMapLanguage(@NonNull MapView mapView, @NonNull Style style, final String language) {
            LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
            localizationPlugin.setMapLanguage(language);
            localizationPlugin.matchMapLanguageWithDeviceDefault();
            return this;
        }

        /**
         * 简体中文
         */
        public Builder setMapDefaultLanguage(@NonNull MapView mapView, @NonNull Style style) {
            setMapLanguage(mapView, style, MapLocale.SIMPLIFIED_CHINESE);
            return this;
        }

        MarkerViewManager markerViewManager;

        public Builder addMarker(@NonNull MapView mapView, @NonNull final LatLng latlng, @NonNull final View view) {
            if (markerViewManager == null) {
                markerViewManager = new MarkerViewManager(mapView, mapboxMap);
            }

            MarkerView markerView = new MarkerView(latlng, view);
            markerViewManager.addMarker(markerView);

            return this;
        }
    }

    public interface OnMapReady {
        void onMapReady(@NonNull MapboxMap map, @NonNull Style style);
    }

    static final class CurrentLocationEngineCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<LocationComponent> componentWeakReference;
        private final WeakReference<OnLocationChangeListener> listenerWeakReference;

        public CurrentLocationEngineCallback(LocationComponent component,
                                             OnLocationChangeListener listener) {
            this.componentWeakReference = new WeakReference<>(component);
            this.listenerWeakReference = new WeakReference<>(listener);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            LocationComponent component = componentWeakReference.get();
            if (component != null) {
                updateLocation(component, result.getLastLocation(), false);
            }
            OnLocationChangeListener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onLocationChange(result.getLastLocation(), true);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            L.e("Failed to obtain location update " + exception);
        }
    }

    static final class LastLocationEngineCallback implements LocationEngineCallback<LocationEngineResult> {
        private final WeakReference<LocationComponent> componentWeakReference;
        private final WeakReference<OnLocationChangeListener> listenerWeakReference;

        LastLocationEngineCallback(LocationComponent component,
                                   OnLocationChangeListener listener) {
            this.componentWeakReference = new WeakReference<>(component);
            this.listenerWeakReference = new WeakReference<>(listener);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            LocationComponent component = componentWeakReference.get();
            if (component != null) {
                updateLocation(component, result.getLastLocation(), true);
            }
            OnLocationChangeListener listener = listenerWeakReference.get();
            if (listener != null) {
                listener.onLocationChange(result.getLastLocation(), true);
            }
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            L.e("Failed to obtain last location update " + exception);
        }
    }
}
