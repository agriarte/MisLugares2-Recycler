package com.example.pedro.mislugares;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static Lugares lugares = new LugaresVector();
    private RecyclerView recyclerView;
    public AdaptadorLugares adaptador;
    private RecyclerView.LayoutManager layoutManager;
    private LocationManager manejador;
    private Location mejorLocalizacion;
    private final int SOLICITUD_PERMISO_LOCALIZACION = 10;
    private static final long DOS_MINUTOS = 2 * 60 * 1000;

    //para actualizar el Recycler notifyDataSetChanged
    @Override
    protected void onRestart() {
        super.onRestart();
        adaptador.notifyDataSetChanged();
        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adaptador = new AdaptadorLugares(this, lugares);
        recyclerView.setAdapter(adaptador);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VistaLugarActivity.class);
                //getChildAdapterPosition() nos indicará la posición de una vista dentro del adaptador
                intent.putExtra("id", (long) recyclerView.getChildAdapterPosition(view));
                startActivity(intent);
            }
        });

        //gps
        manejador = (LocationManager) getSystemService(LOCATION_SERVICE);
        ultimaLocalizacion();

    }

    private void ultimaLocalizacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                actualizaMejorLocalizacion(manejador.getLastKnownLocation(LocationManager.GPS_PROVIDER));
            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                actualizaMejorLocalizacion(manejador.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
            }
        } else {
            solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, "Sin el permiso Localizacion" +
                    " no puedo mostrar las distancias", SOLICITUD_PERMISO_LOCALIZACION, this);
        }
    }

    public static void solicitarPermiso(final String permiso, String justificacion, final int requesCode,
                                        final MainActivity mainActivity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, permiso)) {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Solicitud de Permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(mainActivity, new String[]{permiso}, requesCode);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(mainActivity, new String[]{permiso}, requesCode);
        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ultimaLocalizacion();
                activarProveedores();
                //actualiza reciclerView
                adaptador.notifyDataSetChanged();
            }
        }
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        activarProveedores();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manejador.removeUpdates((LocationListener) this);
        }
    }

    private void activarProveedores() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (manejador.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        20 * 1000, 5, (LocationListener) this);

            }
            if (manejador.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                manejador.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        10 * 1000, 10, (LocationListener) this);
            }
        } else {
            solicitarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, "Sin el " +
                            "permiso Localicazación no puedo mostrar la distancia",
                    SOLICITUD_PERMISO_LOCALIZACION, this);
        }
    }


    private void actualizaMejorLocalizacion(Location lastKnownLocation) {
        if (lastKnownLocation != null && (mejorLocalizacion == null
                || lastKnownLocation.getAccuracy() < 2 * mejorLocalizacion.getAccuracy()
                || lastKnownLocation.getTime() - mejorLocalizacion.getTime() > DOS_MINUTOS)) {
            Log.d(Lugares.TAG, "Nueva mejor localización");
            mejorLocalizacion = lastKnownLocation;
            Lugares.posicionActual.setLatitud(lastKnownLocation.getLatitude());
            Lugares.posicionActual.setLongitud(lastKnownLocation.getLongitude());
        }
    }


    private void lanzarAcercade() {
        Intent intent = new Intent(this, AcercadeActivity.class);
        startActivity(intent);
    }

    private void lanzarPreferencias() {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        startActivity(intent);
    }

    public void mostrarPreferencias() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String s = "Notificaciones: " + preferences.getBoolean("notificaciones", true) +
                ", máximo a listar: " + preferences.getString("maximo", "?") +
                " orden: " + preferences.getString("orden", "?");
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    public void lanzarVistaLugar(View view) {
        final EditText entrada = new EditText(this);
        entrada.setText("0");
        new AlertDialog.Builder(this)
                .setTitle("Selección de lugar")
                .setMessage("indica su id:")
                .setView(entrada)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        long id = Long.parseLong(entrada.getText().toString());
                        Intent i = new Intent(MainActivity.this,
                                VistaLugarActivity.class);
                        i.putExtra("id", id);
                        startActivity(i);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.preferencias) {
            lanzarPreferencias();
            return true;
        }

        if (id == R.id.acercaDe) {
            lanzarAcercade();
        }

        if (id == R.id.menu_buscar) {
            lanzarVistaLugar(null);
            return true;
        }

        if (id==R.id.menu_mapa) {
            Intent intent = new Intent(this, mapaActivity.class);
            startActivity(intent);
        }



        return super.onOptionsItemSelected(item);
    }


    //4 metodos de interfaz
    //cuando la actualizamos cambie la posición y cuando cambie el estado
    //tratamos de activar nuevos proveedores.
    @Override
    public void onLocationChanged(Location location) {
        Log.d(Lugares.TAG, "nueva Localizazcion: " + location);
        actualizaMejorLocalizacion(location);
        adaptador.notifyDataSetChanged();
    }

    @Override
    public void onProviderDisabled(String proveedor) {
        Log.d(Lugares.TAG, "se deshabilita: " + proveedor);
        activarProveedores();
    }

    @Override
    public void onProviderEnabled(String proveedor) {
        Log.d(Lugares.TAG, "se habilita: " + proveedor);
        activarProveedores();
    }

    @Override
    public void onStatusChanged(String proveedor, int estado, Bundle extras) {
        Log.d(Lugares.TAG, "Cambia estodo: " + proveedor);
        activarProveedores();
    }


}
