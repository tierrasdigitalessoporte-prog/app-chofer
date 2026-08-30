package com.tierrasdigitales.transporteescolar.chofer;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

// Puente entre la página web de "App Chofer" (que ya corre dentro de esta
// app, ver capacitor.config.ts) y el servicio nativo RastreoService, que es
// el que de verdad manda la ubicación en segundo plano, aunque el chofer
// se cambie a otra app (WhatsApp, Waze) o se le apague la pantalla — algo
// que una página web sola nunca puede hacer, sin importar qué tan bien
// esté programada.
//
// Desde la página, en JavaScript, se usa así:
//   Capacitor.Plugins.RastreoSegundoPlano.iniciar({ rutaId: 4 })
//   Capacitor.Plugins.RastreoSegundoPlano.detener()
//
// Nota sobre permisos: en Android no se puede pedir el permiso de
// ubicación normal y el de "todo el tiempo" (segundo plano) en el mismo
// aviso — el sistema lo ignora si se intenta. Por eso aquí se piden en dos
// pasos: primero el normal (obligatorio para arrancar), y ya con ese
// concedido, se pide aparte el de segundo plano (best-effort — aunque lo
// niegue, el servicio arranca igual, porque al ser un "foreground service"
// con su notificación fija, Android ya deja que siga mandando ubicación
// aunque el chofer cambie de pantalla).
@CapacitorPlugin(
    name = "RastreoSegundoPlano",
    permissions = {
        @Permission(
            strings = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
            alias = "ubicacion"
        ),
        @Permission(strings = { Manifest.permission.ACCESS_BACKGROUND_LOCATION }, alias = "ubicacionSegundoPlano"),
    }
)
public class RastreoPlugin extends Plugin {

    @PluginMethod
    public void iniciar(PluginCall call) {
        if (getPermissionState("ubicacion") != PermissionState.GRANTED) {
            requestPermissionForAlias("ubicacion", call, "onUbicacionLista");
            return;
        }
        continuarConSegundoPlano(call);
    }

    @PermissionCallback
    private void onUbicacionLista(PluginCall call) {
        if (getPermissionState("ubicacion") != PermissionState.GRANTED) {
            call.reject("Sin permiso de ubicación no se puede compartir la ruta con los papás.");
            return;
        }
        continuarConSegundoPlano(call);
    }

    // Ya con el permiso normal concedido, se pide aparte (en su propio
    // aviso del sistema) el de "todo el tiempo". No bloquea el arranque:
    // se pida o no se conceda, el rastreo arranca de todos modos.
    private void continuarConSegundoPlano(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && getPermissionState("ubicacionSegundoPlano") != PermissionState.GRANTED) {
            requestPermissionForAlias("ubicacionSegundoPlano", call, "onSegundoPlanoListo");
            return;
        }
        arrancarServicio(call);
    }

    @PermissionCallback
    private void onSegundoPlanoListo(PluginCall call) {
        arrancarServicio(call);
    }

    private void arrancarServicio(PluginCall call) {
        Integer rutaId = call.getInt("rutaId");
        if (rutaId == null) {
            call.reject("Falta rutaId.");
            return;
        }
        Intent intent = new Intent(getContext(), RastreoService.class);
        intent.putExtra(RastreoService.EXTRA_RUTA_ID, rutaId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getContext().startForegroundService(intent);
        } else {
            getContext().startService(intent);
        }
        JSObject ret = new JSObject();
        ret.put("ok", true);
        ret.put("segundoPlanoConcedido", getPermissionState("ubicacionSegundoPlano") == PermissionState.GRANTED);
        call.resolve(ret);
    }

    @PluginMethod
    public void detener(PluginCall call) {
        getContext().stopService(new Intent(getContext(), RastreoService.class));
        JSObject ret = new JSObject();
        ret.put("ok", true);
        call.resolve(ret);
    }
}
