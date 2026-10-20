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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RegistroActivity (RF02 y RF04 - Commit 3):
 * Incorpora la obtencion de coordenadas reales mediante FusedLocationProviderClient
 * y la gestion completa de permisos de ubicacion en tiempo de ejecucion.
 */
public class RegistroActivity extends AppCompatActivity {

    // Constante para identificar nuestra solicitud de permiso de GPS
    private static final int CODIGO_PERMISO_UBICACION = 1001;

    private EditText etTitulo, etDescripcion, etFechaHora, etLatitud, etLongitud;
    private Spinner spCategoria;
    private Button btnObtenerGps, btnGuardarIncidencia;

    // Cliente de ubicacion de Google Play Services (RF04)
    private FusedLocationProviderClient fusedLocationClient;

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

        // 2. Inicializar el cliente de ubicación GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 3. Configurar el Spinner de categorias
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

        // 5. Boton para obtener ubicacion GPS verificando permisos (RF04)
        btnObtenerGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarPermisosYObtenerUbicacion();
            }
        });

        // 6. Boton Guardar (valida campos; en Commit 4 se conecta a Firestore)
        btnGuardarIncidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    Toast.makeText(RegistroActivity.this,
                            "Datos y coordenadas validados correctamente.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Comprueba si el usuario ya concedio el permiso de ubicacion.
     * Si no lo ha concedido, muestra el cuadro de dialogo del sistema solicitandolo.
     */
    private void verificarPermisosYObtenerUbicacion() {
        int permisoFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permisoCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permisoFine == PackageManager.PERMISSION_GRANTED || permisoCoarse == PackageManager.PERMISSION_GRANTED) {
            // Ya tenemos permiso concedido: procedemos a leer el GPS
            obtenerCoordenadasGps();
        } else {
            // No tenemos permiso: lo pedimos en tiempo de ejecucion al usuario
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

    /**
     * Obtiene la ubicacion del dispositivo usando FusedLocationProviderClient.
     * Incluye contingencia si getLastLocation() devuelve null (GPS apagado o sin cache previa).
     */
    private void obtenerCoordenadasGps() {
        // Verificacion de seguridad requerida por el compilador de Android
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
                            // Caso exitoso: tenemos una ubicacion valida en cache
                            mostrarUbicacionEnFormulario(location);
                        } else {
                            // CONTINGENCIA: Si getLastLocation() es null (ej. recien se activo el GPS),
                            // forzamos una lectura activa en tiempo real con getCurrentLocation()
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

    /**
     * Solicita una coordenada fresca al hardware GPS cuando getLastLocation() es null.
     * Si el GPS del teléfono está fisicamente apagado, location volvera a ser null y se avisa al usuario.
     */
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
                            //El GPS esta apagado o sin cobertura
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
                                "Fallo al solicitar ubicación en tiempo real: " + e.getMessage());
                    }
                });
    }

    /**
     * Escribe la latitud y longitud obtenidas en los EditText bloqueados del formulario.
     */
    private void mostrarUbicacionEnFormulario(Location location) {
        etLatitud.setError(null);
        etLatitud.setText(String.valueOf(location.getLatitude()));
        etLongitud.setText(String.valueOf(location.getLongitude()));
        Toast.makeText(this, "Ubicación GPS obtenida con éxito", Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback que recibe la respuesta del usuario al cuadro de dialogo de permisos (RF04).
     * Maneja explicitamente cuando el usuario DENIEGA el permiso.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISO_UBICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ACEPTO el permiso
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                obtenerCoordenadasGps();
            } else {
                // CONTINGENCIA OBLIGATORIA: El usuario DENEGO el permiso
                mostrarAlertaError(
                        "Permiso de Ubicación Denegado",
                        "Has rechazado el permiso de GPS. GeoApp necesita tu ubicación para " +
                        "georreferenciar la incidencia en terreno. Puedes volver a presionar 'Obtener Ubicación GPS' " +
                        "para conceder el permiso sin que la aplicación se cierre."
                );
            }
        }
    }

    /**
     * Muestra un cuadro de dialogo informativo para que el usuario entienda cualquier contingencia
     * sin que la aplicación sufra cierres inesperados (crashes).
     */
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
