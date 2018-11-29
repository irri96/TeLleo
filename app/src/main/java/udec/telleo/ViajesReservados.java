package udec.telleo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import udec.telleo.apiclient.TeLleoService;
import udec.telleo.model.Reserva;
import udec.telleo.model.Viaje;

public class ViajesReservados extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viajes_reservados);
        /*Intent i = getIntent();
        String origen = i.getStringExtra("origen");
        String destino = i.getStringExtra("destino");
        String finicio = i.getStringExtra("finicio");
        String ffinal = i.getStringExtra("ffinal");*/
        obtenerReservas();
    }

    private void obtenerReservas(){
        SharedPreferences sp = getSharedPreferences("datos", MODE_PRIVATE);
        Call<List<Reserva>> call = TeLleoService.getService(ViajesReservados.this).
                getReservaPasajero(sp.getString("usuario", ""));
        Log.v("call",call.request().url().toString());
        call.enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                if(!response.isSuccessful()){
                    Log.e("response","fallo "+response.code());

                }
                Log.d("respuesta","  "+response.body().size());
                LinearLayout ll = findViewById(R.id.llayout);
                ll.removeAllViewsInLayout();
                findViewById(R.id.progressBar).setClickable(false);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                findViewById(R.id.textobuscando).setClickable(false);
                findViewById(R.id.textobuscando).setVisibility(View.GONE);
                boolean first=true;
                for(Reserva r : response.body()){
                    if(!first){
                        View v = new View(ViajesReservados.this);
                        v.setLayoutParams(
                                new LinearLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        5
                                )
                        );
                        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
                    }
                    first = false;
                    Log.d("reserva:", r.toString());
                    View child = getLayoutInflater().inflate(R.layout.fragment_viajes_reservados,null);
                    if(r.getEstado() == 1){
                        child.setBackgroundColor(getResources().getColor(R.color.LightGoldenrodYellow));
                    }
                    else{
                        child.setBackgroundColor(getResources().getColor(R.color.LightGreen));
                    }
                    ((TextView)child.findViewById(R.id.origen)).setText(r.getOrigen());
                    ((TextView)child.findViewById(R.id.destino)).setText(r.getDestino());
                    ((TextView)child.findViewById(R.id.fecha)).setText(r.getHora().toString());
                    ((TextView)child.findViewById(R.id.precio)).setText("$" + r.getPrecio());

                    child.findViewById(R.id.CancelButton)
                            .setOnClickListener(new ViajesReservados.ReservaClickListener(r.getPasajero(),r.getId()));
                    ll.addView(child);
                }
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                Log.d("ERROR", t.toString());
            }
        });
    }

    private class ReservaClickListener implements View.OnClickListener{
        private String r;
        private int id;
        public ReservaClickListener(String Pasajero,int idReserva){
            r=Pasajero;
            id=idReserva;
        }
        @Override
        public void onClick(View view) {
            //borrar reserva en api
            Call<ResponseBody> call = TeLleoService.getService(ViajesReservados.this).
                    deleteReserva(r,id);
            Log.v("call",call.request().url().toString());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(ViajesReservados.this,"Reserva cancelada",Toast.LENGTH_LONG).show();
                    obtenerReservas();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("ERROR", t.toString());
                }
            });

        }
    }
}
