package udec.telleo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import udec.telleo.apiclient.TeLleoService;
import udec.telleo.model.Reserva;
import udec.telleo.model.Viaje;

public class ElegirViaje extends AppCompatActivity {
    private EditText ti,tf;
    private PlaceAutocompleteFragment pafo,pafd;
    private Place[] orDes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegir_viaje);
        orDes = new Place[2];
        orDes[0]=orDes[1]=null;
        ti = this.findViewById(R.id.fechainicio);
        ti.setOnClickListener(new FechaListener(ti));
        tf = this.findViewById(R.id.fechafinal);
        tf.setOnClickListener(new FechaListener(tf));
        pafo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.forigen);
        pafd = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.fdestino);
        AutocompleteFilter typeFilter1 = new AutocompleteFilter.Builder().setCountry("CL")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
        AutocompleteFilter typeFilter2 = new AutocompleteFilter.Builder().setCountry("CL")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES).build();
        pafo.setFilter(typeFilter1);
        pafd.setFilter(typeFilter2);
        pafo.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pafo.setText("");
                        view.setVisibility(View.GONE);
                        orDes[0]=null;
                    }
                }
        );
        pafd.getView().findViewById(R.id.place_autocomplete_clear_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pafd.setText("");
                        view.setVisibility(View.GONE);
                        orDes[1]=null;
                    }
                }
        );
        pafo.setOnPlaceSelectedListener(new TextoListener(orDes,true));
        pafd.setOnPlaceSelectedListener(new TextoListener(orDes,false));
        ((EditText)pafo.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Origen");

        ((EditText)pafd.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Destino");
        Button b = findViewById(R.id.button);
        b.setOnClickListener(new BotonListener());
    }

    private class BotonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if(orDes[0]==null){
                Toast.makeText(ElegirViaje.this,"Ingrese origen",Toast.LENGTH_LONG).show();
                return;
            }
            if(orDes[1]==null){
                Toast.makeText(ElegirViaje.this,"Ingrese destino",Toast.LENGTH_LONG).show();
                return;
            }
            if(ti.getText().toString().equals("") || tf.getText().toString().equals("")){
                Toast.makeText(ElegirViaje.this,"Ingrese fechas",Toast.LENGTH_LONG).show();
                return;
            }
            Log.v("ALO","VOY A BUSCAR");

            Log.v("origen", (String) orDes[0].getName());
            SimpleDateFormat dmy = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
            String finicio = null,ffinal=null;
            try {
                finicio = ymd.format(dmy.parse(ti.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                ffinal = ymd.format(dmy.parse(tf.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Call<List<Viaje>> call = TeLleoService.getService(ElegirViaje.this).
                    getViajes(orDes[0].getName().toString(),orDes[1].getName().toString(),
                            finicio+"T00:00:00.000Z",ffinal+"T23:59:59.000Z",1,0);
            Log.v("call",call.request().url().toString());
            call.enqueue(new Callback<List<Viaje>>() {
                @Override
                public void onResponse(Call<List<Viaje>> call, Response<List<Viaje>> response) {
                    if(!response.isSuccessful()){
                        Log.e("response","fallo "+response.code());

                    }
                    Log.d("respuesta","  "+response.toString());
                    for(Viaje r : response.body()){
                        Log.d("reserva:", r.toString());
                    }
                }

                @Override
                public void onFailure(Call<List<Viaje>> call, Throwable t) {
                    Log.d("ERROR", t.toString());
                }
            });
            Log.v("asd",ti.getText().toString());

        }
    }



    private class FechaListener implements View.OnClickListener{
        private EditText t;
        FechaListener(EditText et){
            t=et;
        }
        @Override
        public void onClick(View view) {

            Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            int mm = calendar.get(Calendar.MONTH);
            DatePickerDialog dpd = new  DatePickerDialog(ElegirViaje.this,
                    AlertDialog.THEME_HOLO_DARK,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                            String day = String.format("%02d",d);
                            String year = String.format("%d",y);
                            String month = String.format("%02d",m+1);
                            t.setText(day+"/"+month+"/"+year);
                        }
                    }
            ,yy,mm,dd);
            dpd.show();
        }
    }
    private class TextoListener implements PlaceSelectionListener{
        private Place[] orDes;
        private boolean esOr;
        TextoListener(Place[] par,boolean v){
            orDes=par;
            esOr=v;
        }
        @Override
        public void onPlaceSelected(Place place) {
            if(esOr) {
                orDes[0]=place;
            } else {
                orDes[1]=place;
            }
            Log.v("ALO","LO CAMBIE A "+place.getName() );
        }

        @Override
        public void onError(Status status) {
            Log.e("Error",status.getStatusMessage());
        }
    }
}
