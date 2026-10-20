package cl.heredia.geoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ListadoActivity (RF03 - Commit 4):
 * Consulta los documentos de la coleccion "incidencias" en Firebase Cloud Firestore
 * y los presenta en pantalla mediante un RecyclerView, controlando errores de red.
 */
public class ListadoActivity extends AppCompatActivity {

    private RecyclerView rvIncidencias;
    private TextView tvEstadoListado;
    private IncidenciaAdapter adapter;
    private List<Incidencia> listaIncidencias;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado);

        // 1. Vincular componentes visuales
        rvIncidencias = findViewById(R.id.rvIncidencias);
        tvEstadoListado = findViewById(R.id.tvEstadoListado);

        // 2. Configurar el RecyclerView con un LinearLayoutManager vertical
        rvIncidencias.setLayoutManager(new LinearLayoutManager(this));
        listaIncidencias = new ArrayList<>();
        adapter = new IncidenciaAdapter(listaIncidencias);
        rvIncidencias.setAdapter(adapter);

        // 3. Inicializar Firebase Firestore y consultar la colección
        db = FirebaseFirestore.getInstance();
        cargarIncidenciasDesdeFirestore();
    }

    /**
     * Consulta la colección "incidencias" en Firestore y transforma cada documento
     * en un objeto Incidencia mediante reflexion automatica (requiere constructor vacio).
     */
    private void cargarIncidenciasDesdeFirestore() {
        db.collection("incidencias")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        listaIncidencias.clear();

                        // Recorrer cada documento retornado por Firestore
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Incidencia inc = doc.toObject(Incidencia.class);
                            inc.setId(doc.getId());
                            listaIncidencias.add(inc);
                        }

                        // Notificar al adaptador que los datos cambiaron para refrescar la lista
                        adapter.notifyDataSetChanged();

                        // Actualizar mensaje de estado segun si la lista tiene elementos o esta vacia
                        if (listaIncidencias.isEmpty()) {
                            tvEstadoListado.setText("No hay incidencias registradas todavía.");
                            tvEstadoListado.setVisibility(View.VISIBLE);
                        } else {
                            tvEstadoListado.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // CONTINGENCIA: Manejo de corte de conexion o fallo de permisos en Firestore
                        tvEstadoListado.setText("Error al cargar datos. Verifique su conexión a Internet.");
                        Toast.makeText(ListadoActivity.this,
                                "Error de conexión: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        new AlertDialog.Builder(ListadoActivity.this)
                                .setTitle("Error de Conexión a la Nube")
                                .setMessage("No fue posible obtener el historial desde Firebase Firestore: " + e.getMessage())
                                .setPositiveButton("Aceptar", null)
                                .show();
                    }
                });
    }
}
