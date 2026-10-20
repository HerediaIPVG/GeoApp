package cl.heredia.geoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * IncidenciaAdapter (RF03 - Commit 4):
 * Adaptador que conecta nuestra lista de objetos en memoria (List<Incidencia>)
 * con el componente visual RecyclerView para reciclar las vistas en pantalla.
 */
public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    private List<Incidencia> listaIncidencias;

    public IncidenciaAdapter(List<Incidencia> listaIncidencias) {
        this.listaIncidencias = listaIncidencias;
    }

    /**
     * 1. onCreateViewHolder:
     * Infla el diseño XML item_incidencia cuando el RecyclerView necesita crear una nueva tarjeta.
     */
    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vistaFila = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidencia, parent, false);
        return new IncidenciaViewHolder(vistaFila);
    }

    /**
     * 2. onBindViewHolder:
     * Enlaza los datos del objeto Incidencia en la posición indicada con los TextViews visuales.
     */
    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia inc = listaIncidencias.get(position);

        holder.tvItemTitulo.setText(inc.getTitulo());
        holder.tvItemCategoriaFecha.setText("Categoría: " + inc.getCategoria() + " | Fecha: " + inc.getFechaHora());
        holder.tvItemDescripcion.setText(inc.getDescripcion());
        holder.tvItemCoordenadas.setText("GPS: Lat " + inc.getLatitud() + " , Lng " + inc.getLongitud());
    }

    /**
     * 3. getItemCount:
     * Informa al RecyclerView la cantidad total de elementos en la lista.
     */
    @Override
    public int getItemCount() {
        return listaIncidencias != null ? listaIncidencias.size() : 0;
    }

    /**
     * Clase interna IncidenciaViewHolder:
     * Almacena las referencias a las vistas hijas de cada fila para evitar llamadas
     * repetitivas e innecesarias a findViewById() durante el scroll, optimizando el rendimiento.
     */
    public static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemTitulo, tvItemCategoriaFecha, tvItemDescripcion, tvItemCoordenadas;

        public IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemTitulo = itemView.findViewById(R.id.tvItemTitulo);
            tvItemCategoriaFecha = itemView.findViewById(R.id.tvItemCategoriaFecha);
            tvItemDescripcion = itemView.findViewById(R.id.tvItemDescripcion);
            tvItemCoordenadas = itemView.findViewById(R.id.tvItemCoordenadas);
        }
    }
}
