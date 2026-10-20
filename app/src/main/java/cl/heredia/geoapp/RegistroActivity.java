package cl.heredia.geoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RegistroActivity (RF02 - Commit 2):
 * Formulario para ingresar los datos de una incidencia, autocompletar la fecha/hora
 * actual del sistema y validar estrictamente que ningún campo obligatorio ni coordenada esté vacío.
 */
public class RegistroActivity extends AppCompatActivity {

    private EditText etTitulo, etDescripcion, etFechaHora, etLatitud, etLongitud;
    private Spinner spCategoria;
    private Button btnObtenerGps, btnGuardarIncidencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 1. Inicializar referencias a las vistas del layout XML
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        spCategoria = findViewById(R.id.spCategoria);
        etFechaHora = findViewById(R.id.etFechaHora);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        btnObtenerGps = findViewById(R.id.btnObtenerGps);
        btnGuardarIncidencia = findViewById(R.id.btnGuardarIncidencia);

        // 2. Poblar el Spinner con las categorias exigidas por los requisitos (RF02)
        String[] categorias = {"Seleccione categoría...", "Vialidad", "Alumbrado", "Basura", "Otro"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapterSpinner);

        // 3. Autocompletar Fecha y Hora actual en formato legible dd/MM/yyyy
        cargarFechaHoraActual();

        // 4. Accion temporal del botón GPS
        btnObtenerGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegistroActivity.this,
                        "El módulo GPS real se integrará en el Commit 3",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Accion del boton Guardar: Primero valida estrictamente todos los campos
        btnGuardarIncidencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    Toast.makeText(RegistroActivity.this,
                            "Validación exitosa. En el Commit 4 se guardará en Cloud Firestore.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Obtiene la fecha y hora del telefono y la escribe en el campo etFechaHora.
     */
    private void cargarFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String fechaActual = sdf.format(new Date());
        etFechaHora.setText(fechaActual);
    }

    /**
     * Metodo validarCampos() (RF02):
     * Verifica que el titulo, descripcion, categoria seleccionada, fecha/hora y coordenadas GPS
     * no esten vacios antes de permitir guardar el registro.
     *
     * @return true si todos los datos son validos; false si falta algún dato.
     */
    private boolean validarCampos() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        int posicionCategoria = spCategoria.getSelectedItemPosition();
        String fechaHora = etFechaHora.getText().toString().trim();
        String latitudStr = etLatitud.getText().toString().trim();
        String longitudStr = etLongitud.getText().toString().trim();

        // Validar Titulo
        if (titulo.isEmpty()) {
            etTitulo.setError("Debe ingresar un título para la incidencia");
            etTitulo.requestFocus();
            return false;
        }

        // Validar Descripcion
        if (descripcion.isEmpty()) {
            etDescripcion.setError("Debe ingresar una descripción del problema");
            etDescripcion.requestFocus();
            return false;
        }

        // Validar que se haya elegido una categoria valida en el Spinner (indice > 0)
        if (posicionCategoria == 0) {
            Toast.makeText(this, "Por favor, seleccione una categoría válida", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar Fecha y Hora
        if (fechaHora.isEmpty()) {
            etFechaHora.setError("La fecha y hora no pueden estar vacías");
            return false;
        }

        // Validar Coordenadas GPS
        if (latitudStr.isEmpty() || longitudStr.isEmpty()) {
            etLatitud.setError("Obtenga las coordenadas GPS primero");
            Toast.makeText(this,
                    "Debe presionar 'Obtener Ubicación GPS' antes de guardar",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
