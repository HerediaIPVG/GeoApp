package cl.heredia.geoapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * MapaActivity (RF05 - Commit 5):
 * Despliega un mapa interactivo de Google Maps mediante SupportMapFragment y OnMapReadyCallback.
 * Lee las incidencias almacenadas en Cloud Firestore y dibuja un marcador (Marker) interactivo por cada una.
 */
public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        db = FirebaseFirestore.getInstance();

        // Obtener el fragmento SupportMapFragment y solicitar notificacion cuando el mapa este listo
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Error al inicializar el fragmento de Google Maps", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Se ejecuta automaticamente cuando el mapa de Google Maps ha terminado de cargarse.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Habilitar controles visuales de zoom (+ / -)
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Posicion inicial por defecto (Concepcion, Chile) mientras se descargan las incidencias
        LatLng posicionPorDefecto = new LatLng(-36.8201, -73.0444);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionPorDefecto, 12f));

        // Consultar Firestore y posicionar los marcadores
        cargarMarcadoresDesdeFirestore();
    }

    /**
     * Lee todos los documentos de la coleccion "incidencias" en Firestore y añade un Marker al mapa.
     */
    private void cargarMarcadoresDesdeFirestore() {
        db.collection("incidencias")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        LatLng ultimaPosicion = null;
                        int contador = 0;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Incidencia inc = doc.toObject(Incidencia.class);
                            LatLng posicion = new LatLng(inc.getLatitud(), inc.getLongitud());

                            // Agregar marcador con titulo y categoria en el snippet
                            mMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(inc.getTitulo())
                                    .snippet("Categoría: " + inc.getCategoria() + " (" + inc.getFechaHora() + ")"));

                            ultimaPosicion = posicion;
                            contador++;
                        }

                        // Si existen incidencias registradas, animamos la camara hacia la ultima reportada
                        if (ultimaPosicion != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ultimaPosicion, 15f));
                            Toast.makeText(MapaActivity.this,
                                    "Se cargaron " + contador + " incidencias en el mapa",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MapaActivity.this,
                                    "No hay incidencias registradas para mostrar en el mapa",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapaActivity.this,
                                "Error al cargar marcadores desde Firestore: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
