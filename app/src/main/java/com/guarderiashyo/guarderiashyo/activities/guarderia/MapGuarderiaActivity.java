package com.guarderiashyo.guarderiashyo.activities.guarderia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.guarderiashyo.guarderiashyo.R;
import com.guarderiashyo.guarderiashyo.activities.InicioActivity;
import com.guarderiashyo.guarderiashyo.activities.client.MapClientActivity;
import com.guarderiashyo.guarderiashyo.providers.AuthProvider;

public class MapGuarderiaActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button btnCerrarSesion;
    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guarderia);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        mAuthProvider = new AuthProvider();
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuthProvider.logout();
                Intent i = new Intent(MapGuarderiaActivity.this, InicioActivity.class);
                startActivity(i);
                finish();
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
