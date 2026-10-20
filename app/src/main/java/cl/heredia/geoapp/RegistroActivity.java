package cl.heredia.geoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RegistroActivity (RF02, RF03 y RF04 - Commit 4):
 * Valida el formulario, obtiene coordenadas GPS y persiste la incidencia en
 * la base de datos NoSQL en la nube Firebase Cloud Firestore.
 */
public class RegistroActivity extends AppCompatActivity {

    private static final int CODIGO_PERMISO_UBICACION = 1001;

    private EditText etTitulo, etDescripcion, etFechaHora, etLatitud, etLongitud;
    private Spinner spCategoria;
    private Button btnObtenerGps, btnGuardarIncidencia;

    private FusedLocationProviderClient fusedLocationClient;
    // Instancia de la base de datos en la nube Cloud Firestore (RF03)
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 1. Vincular vistas del XML
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spCategoria = findViewById(R.id.spCategoria);
        etFechaHora = findViewById(R.id.etFechaHora);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        btnObtenerGps = findViewById(R.id.btnObtenerGps);
        btnGuardarIncidencia = findViewById(R.id.btnGuardarIncidencia);

        // 2. Inicializar servicios de GPS y Firebase Firestore
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        // 3. Configurar Spinner de categorias
        String[] categorias = {"Seleccione categoría...", "Vialidad", "Alumbrado", "Basura", "Otro"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapterSpinner);

        // 4. Autocompletar Fecha y Hora actual
        cargarFechaHoraActual();

        // 5. Boton GPS con solicitud de permisos en tiempo de ejecucion
        btnObtenerGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisosYObtenerUbicacion();
            }
        });

        // 6. Boton Guardar en Firestore (RF03)
        btnGuardarIncidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    guardarEnFirestore();
                }
            }
        });
    }

    /**
     * Envia el objeto Incidencia a la coleccion "incidencias" de Firebase Cloud Firestore.
     * Maneja eventos de exito y de error de red sin congelar ni cerrar la app.
     */
    private void guardarEnFirestore() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String fechaHora = etFechaHora.getText().toString().trim();
        double latitud = Double.parseDouble(etLatitud.getText().toString().trim());
        double longitud = Double.parseDouble(etLongitud.getText().toString().trim());

        // Deshabilitar boton temporalmente para evitar doble envio accidental
        btnGuardarIncidencia.setEnabled(false);
        Toast.makeText(this, "Guardando incidencia en la nube...", Toast.LENGTH_SHORT).show();

        // Crear objeto modelo Incidencia (el ID se generara automaticamente en Firestore)
        Incidencia nuevaIncidencia = new Incidencia(
                null, titulo, descripcion, categoria, fechaHora, latitud, longitud
        );

        // Guardar en la coleccion "incidencias" de Firestore
        db.collection("incidencias")
                .add(nuevaIncidencia)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(RegistroActivity.this,
                                "Incidencia registrada con éxito (ID: " + documentReference.getId() + ")",
                                Toast.LENGTH_LONG).show();
                        // Regresar al menu principal tras guardar exitosamente
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // CONTINGENCIA: Si falla la conexion o las reglas de seguridad rechazan la escritura
                        btnGuardarIncidencia.setEnabled(true);
                        mostrarAlertaError(
                                "Error al Guardar en la Nube",
                                "No se pudo registrar la incidencia en Firestore. " +
                                "Verifique su conexión a Internet o reglas de acceso. Detalle: " + e.getMessage()
                        );
                    }
                });
    }

    private void verificarPermisosYObtenerUbicacion() {
        int permisoFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permisoCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permisoFine == PackageManager.PERMISSION_GRANTED || permisoCoarse == PackageManager.PERMISSION_GRANTED) {
            obtenerCoordenadasGps();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    CODIGO_PERMISO_UBICACION
            );
        }
    }

    private void obtenerCoordenadasGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Obteniendo coordenadas GPS...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mostrarUbicacionEnFormulario(location);
                        } else {
                            solicitarUbicacionActiva();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mostrarAlertaError("Error al leer el GPS",
                                "No fue posible comunicarse con el servicio de ubicación: " + e.getMessage());
                    }
                });
    }

    private void solicitarUbicacionActiva() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mostrarUbicacionEnFormulario(location);
                        } else {
                            mostrarAlertaError(
                                    "GPS Apagado o Sin Señal",
                                    "No se pudo obtener la ubicación (location == null). " +
                                    "Por favor, verifique que el GPS de su teléfono esté encendido e intente nuevamente."
                            );
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mostrarAlertaError("Error de Ubicación Activa",
                                "Fallo al solicitar ubicación fresca: " + e.getMessage());
                    }
                });
    }

    private void mostrarUbicacionEnFormulario(Location location) {
        etLatitud.setError(null);
        etLatitud.setText(String.valueOf(location.getLatitude()));
        etLongitud.setText(String.valueOf(location.getLongitude()));
        Toast.makeText(this, "Ubicación GPS obtenida con éxito", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                obtenerCoordenadasGps();
            } else {
                mostrarAlertaError(
                        "Permiso de Ubicación Denegado",
                        "Has rechazado el permiso de GPS. GeoApp necesita tu ubicación para " +
                        "georreferenciar la incidencia. Puedes volver a presionar 'Obtener Ubicación GPS' " +
                        "para conceder el permiso sin que la aplicación se cierre."
                );
            }
        }
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void cargarFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        etFechaHora.setText(sdf.format(new Date()));
    }

    private boolean validarCampos() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        int posicionCategoria = spCategoria.getSelectedItemPosition();
        String fechaHora = etFechaHora.getText().toString().trim();
        String latitudStr = etLatitud.getText().toString().trim();
        String longitudStr = etLongitud.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Debe ingresar un título para la incidencia");
            etTitulo.requestFocus();
            return false;
        }
        if (descripcion.isEmpty()) {
            etDescripcion.setError("Debe ingresar una descripción del problema");
            etDescripcion.requestFocus();
            return false;
        }
        if (posicionCategoria == 0) {
            Toast.makeText(this, "Por favor, seleccione una categoría válida", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fechaHora.isEmpty()) {
            etFechaHora.setError("La fecha y hora no pueden estar vacías");
            return false;
        }
        if (latitudStr.isEmpty() || longitudStr.isEmpty()) {
            etLatitud.setError("Obtenga las coordenadas GPS primero");
            Toast.makeText(this, "Debe presionar 'Obtener Ubicación GPS' antes de guardar", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
