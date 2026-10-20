package cl.heredia.geoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * MainActivity (RF01 y RF06 - Commit 5):
 * Ademas de la navegacion principal, implementa SensorEventListener para monitorear
 * en tiempo real el sensor Acelerometro (Sensor.TYPE_ACCELEROMETER), desplegando los ejes
 * X, Y, Z y detectando sacudidas o movimientos bruscos en terreno (shake).
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button btnIrRegistro, btnIrListado, btnIrMapa;
    private TextView tvValoresSensor, tvEstadoSacudida;

    // Variables para la gestion del hardware acelerómetro (RF06)
    private SensorManager sensorManager;
    private Sensor acelerometro;

    // Umbral de aceleracion (en m/s²) para considerar una sacudida brusca.
    // La gravedad terrestre en reposo es ~9.8 m/s²; un movimiento superior a 15.0 m/s² indica sacudida.
    private static final float UMBRAL_SACUDIDA = 15.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vincular componentes visuales
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        btnIrListado = findViewById(R.id.btnIrListado);
        btnIrMapa = findViewById(R.id.btnIrMapa);
        tvValoresSensor = findViewById(R.id.tvValoresSensor);
        tvEstadoSacudida = findViewById(R.id.tvEstadoSacudida);

        // 2. Configurar Intents de navegacion principal
        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistroActivity.class));
            }
        });

        btnIrListado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListadoActivity.class));
            }
        });

        btnIrMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapaActivity.class));
            }
        });

        // 3. Inicializar SensorManager y obtener el Acelerometro por defecto
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // CONTINGENCIA OBLIGATORIA: Manejo de dispositivos o emuladores sin acelerometro físico
        if (acelerometro == null) {
            tvValoresSensor.setText("Sensor no disponible en este dispositivo");
            tvEstadoSacudida.setText("Estado: Sin sensor acelerómetro físico");
            tvEstadoSacudida.setTextColor(Color.GRAY);
        }
    }

    /**
     * Ciclo de vida onResume():
     * Se activa el listener del acelerometro unicamente cuando la Activity esta visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * Ciclo de vida onPause():
     * Se apaga el listener del acelerometro para ahorrar bateria cuando la Activity pasa a segundo plano.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (acelerometro != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Se invoca automaticamente cuando el sensor detecta cambios en alguno de sus 3 ejes (X, Y, Z).
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Presentar valores de los ejes X, Y, Z formateados con 2 decimales
            String textoEjes = String.format(Locale.getDefault(),
                    "Ejes X: %.2f | Y: %.2f | Z: %.2f m/s²", x, y, z);
            tvValoresSensor.setText(textoEjes);

            // Magnitud del vector de aceleracion resultante: raiz cuadrada de (x² + y² + z²)
            double magnitudAceleracion = Math.sqrt((x * x) + (y * y) + (z * z));

            // Comparar contra el umbral para detectar sacudida
            if (magnitudAceleracion > UMBRAL_SACUDIDA) {
                tvEstadoSacudida.setText("¡ALERTA! Movimiento brusco en terreno detectado");
                tvEstadoSacudida.setTextColor(Color.parseColor("#C62828")); // Color rojo alerta
                Toast.makeText(this, "Movimiento brusco en terreno detectado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se requiere logica adicional cuando varia la precision del sensor
    }
}
