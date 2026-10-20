package cl.heredia.geoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity (RF01 - Commit 2):
 * Gestiona el menú principal de la aplicación y la navegación mediante Intents explícitos
 * hacia las pantallas de Registro, Listado y Mapa.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnIrRegistro, btnIrListado, btnIrMapa;
    private TextView tvValoresSensor, tvEstadoSacudida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vincular componentes visuales del XML con las variables Java
        btnIrRegistro = findViewById(R.id.btnIrRegistro);
        btnIrListado = findViewById(R.id.btnIrListado);
        btnIrMapa = findViewById(R.id.btnIrMapa);
        tvValoresSensor = findViewById(R.id.tvValoresSensor);
        tvEstadoSacudida = findViewById(R.id.tvEstadoSacudida);

        // 2. Configurar navegación hacia RegistroActivity usando un Intent explícito
        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        // 3. Configurar navegación hacia ListadoActivity
        btnIrListado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListadoActivity.class);
                startActivity(intent);
            }
        });

        // 4. Configurar navegación hacia MapaActivity
        btnIrMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapaActivity.class);
                startActivity(intent);
            }
        });
    }
}
