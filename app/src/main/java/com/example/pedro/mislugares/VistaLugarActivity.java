package com.example.pedro.mislugares;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Pedro on 22/12/2017.
 */


public class VistaLugarActivity extends AppCompatActivity {
    private long id;
    private Lugar lugar;
    final static int RESULTADO_EDITAR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);

        Bundle extras = getIntent().getExtras();

        id = extras.getLong("id", -1);

        actualizarVistas();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vista_lugar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.accion_compartir:
                return true;
            case R.id.accion_borrar:
                borrarLugar((int) id);
                return true;
            case R.id.accion_editar:
                editarLugar((int) id);
                return true;
            case R.id.accion_llegar:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }


    /**
     * Metodo que recibe datos devueltos de la activividad llamada. Al volver de la otra actividad
     * se ejecuta el metodo onActivityResult. Recibe los datos requestcode y resultCode.
     * En este ejemplo no hay resultCode
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULTADO_EDITAR) {
            actualizarVistas();
            findViewById(R.id.scrollView1).invalidate();
        }
    }

    public void editarLugar(final int id) {
        Intent intent = new Intent(getApplicationContext(),EdicionLugarActivity.class);
        intent.putExtra("id",id);
        startActivityForResult(intent, RESULTADO_EDITAR);
    }

    public void borrarLugar(final int id) {

        new AlertDialog.Builder(this).
                setTitle("Borrado de Lugar").
                setMessage("¿estas seguro que quieres borrar el lugar?").
                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        MainActivity.lugares.borrar(id);
                        finish();
                    }
                }).setNegativeButton("Cancelar", null).
                show();

    }

    private void actualizarVistas(){

        lugar = MainActivity.lugares.elemento((int) id);

        TextView nombre = (TextView) findViewById(R.id.nombre);
        nombre.setText(lugar.getNombre());
        ImageView logo_tipo = (ImageView) findViewById(R.id.logo_tipo);
        logo_tipo.setImageResource(lugar.getTipo().getRecurso());
        TextView tipo = (TextView) findViewById(R.id.tipo);
        tipo.setText(lugar.getTipo().getTexto());
        TextView direccion = (TextView) findViewById(R.id.direccion);
        direccion.setText(lugar.getDireccion());
        TextView telefono = (TextView) findViewById(R.id.telefono);
        telefono.setText(Integer.toString(lugar.getTelefono()));
        TextView url = (TextView) findViewById(R.id.url);
        url.setText(lugar.getUrl());
        TextView comentario = (TextView) findViewById(R.id.comentario);
        comentario.setText(lugar.getComentario());
        TextView fecha = (TextView) findViewById(R.id.fecha);
        fecha.setText(DateFormat.getDateInstance().format(
                new Date(lugar.getFecha())));
        TextView hora = (TextView) findViewById(R.id.hora);
        hora.setText(DateFormat.getTimeInstance().format(
                new Date(lugar.getFecha())));
        RatingBar valoracion = (RatingBar) findViewById(R.id.valoracion);
        valoracion.setRating(lugar.getValoracion());
        valoracion.setOnRatingBarChangeListener(
                new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar,
                                                float valor, boolean fromUser) {
                        lugar.setValoracion(valor);
                    }
                });

    }
}



