package com.tierrasdigitales.transporteescolar.chofer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RastreoService extends Service {

    public static final String EXTRA_RUTA_ID = "rutaId";

    private static final String URL_ACTUALIZAR_UBICACION =
        "https://transporte-escolar.tierras-digitales.com/transporte-escolar/ajax/chofer_actualizar_ubicacion.php";
    private static final long INTERVALO_MS = 12000;
    private static final String CANAL_ID = "rastreo_ruta";
    private static final int NOTIFICACION_ID = 4821;

    private HandlerThread hiloTrabajo;
    private Handler manejador;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private int rutaId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        hiloTrabajo = new HandlerThread("RastreoServiceThread");
        hiloTrabajo.start();
        manejador = new Handler(hiloTrabajo.getLooper());
        crearCanalNotificacion();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rutaId = intent != null ? intent.getIntExtra(EXTRA_RUTA_ID, 0) : 0;

        Notification notificacion = construirNotificacion();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICACION_ID, notificacion, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICACION_ID, notificacion);
        }

        iniciarActualizacionesUbicacion();
        return START_STICKY;
    }

    private void iniciarActualizacionesUbicacion() {
        boolean tienePreciso = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean tieneAproximado = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!tienePreciso && !tieneAproximado) {
            stopSelf();
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mandarUbicacion(location.getLatitude(), location.getLongitude());
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(String provider) {}
            @Override public void onProviderDisabled(String provider) {}
        };
        try {
            if (tienePreciso && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVALO_MS, 0, locationListener, hiloTrabajo.getLooper());
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVALO_MS, 0, locationListener, hiloTrabajo.getLooper());
            }
        } catch (SecurityException e) {
            stopSelf();
        }
    }

    private void mandarUbicacion(double lat, double lng) {
        manejador.post(() -> {
            HttpURLConnection conn = null;
            try {
                String cuerpo = "{\"ruta_id\":" + rutaId + ",\"lat\":" + lat + ",\"lng\":" + lng + "}";
                URL url = new URL(URL_ACTUALIZAR_UBICACION);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(cuerpo.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
            } catch (Exception e) {
                // Sin conexión momentánea, etc.
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                CANAL_ID, "Ruta en curso", NotificationManager.IMPORTANCE_LOW
            );
            canal.setDescription("Se muestra mientras compartes tu ubicación con los papás durante una ruta.");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(canal);
        }
    }

    private Notification construirNotificacion() {
        Intent abrirApp = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pendingIntent = null;
        if (abrirApp != null) {
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0);
            pendingIntent = PendingIntent.getActivity(this, 0, abrirApp, flags);
        }
        return new NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("Compartiendo tu ubicación")
            .setContentText("Los papás pueden ver que vas en camino.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build();
    }

    @Override
    public void onDestroy() {
        if (locationManager != null && locationListener != null) {
            try { locationManager.removeUpdates(locationListener); } catch (SecurityException ignored) {}
        }
        if (hiloTrabajo != null) hiloTrabajo.quitSafely();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}