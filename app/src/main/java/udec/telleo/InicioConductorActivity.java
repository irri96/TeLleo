package udec.telleo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class InicioConductorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_conductor);
    }

    public void verViajesCreadosClick(View view) {
        Intent intent = new Intent(this, ViajesCreadosActivity.class);
        startActivity(intent);
    }

    public void crearViajeClick(View view) {
        Intent intent = new Intent(this, crearviaje.class);
        startActivity(intent);
    }
}
