package net.ra1n_entertainment.landmarkremark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Mudasar Javed on 25/1/18.
 */

public class PickUsername extends Activity {

    /**
     * Pretty straight forward
     * @param savedInstanceState - savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pick_username);

        final EditText UsernameInput = findViewById(R.id.pickUsernameUsernameInput);
        Button ContinueButton = findViewById(R.id.pickUsernameContinueButton);

        ContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = UsernameInput.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(PickUsername.this, "Enter a username", Toast.LENGTH_SHORT).show();
                } else {
                    Intent main = new Intent(PickUsername.this, MainActivity.class);
                    main.putExtra("username", username);
                    startActivity(main);
                }
            }
        });

    }
}
