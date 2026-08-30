package com.tierrasdigitales.transporteescolar.chofer;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(RastreoPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
