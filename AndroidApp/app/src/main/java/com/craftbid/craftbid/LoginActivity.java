package com.craftbid.craftbid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    private EditText username_edit, password_edit;
    private boolean creator;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username_edit = findViewById(R.id.username);
        password_edit = findViewById(R.id.password);
    }

    public void openSignupCustomer(View view) {
        Intent signupCustomer = new Intent(LoginActivity.this, SignupCustomerActivity.class);
        startActivity(signupCustomer);
    }

    public void openSignupCreator(View view) {
        Intent signupCreator = new Intent(LoginActivity.this, SignupCreatorActivity.class);
        startActivity(signupCreator);
    }

    //Open Main As Guest
    public void openMain(View view){
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        main.putExtra("username", MainActivity.GUEST);
        main.putExtra("creator", false);
        startActivity(main);
    }

    public void attemptLogin(View view) {
        username = username_edit.getText().toString();
        String password = password_edit.getText().toString();
        new AttemptLoginTask().execute(username, password);
    }

    /** Connects to server to verify user's credentials */
    private class AttemptLoginTask extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDialog;
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String response, resultmsg;
        boolean is_successful = false;

        @Override
        protected Void doInBackground(String... credentials) {
            try {
                socket = new Socket(NetInfo.getServer_ip(),NetInfo.getServer_port());
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject("LOGIN");
                out.writeObject(credentials[0]); //username
                out.writeObject(credentials[1]); //password

                response = (String) in.readObject();
                switch (response) {
                    case "LOGIN SUCCESSFUL":
                        creator = (boolean) in.readObject();
                        is_successful = true;
                        break;
                    case "WRONG PASSWORD":
                        resultmsg = "Λάθος κωδικός.";
                        break;
                    case "WRONG USERNAME":
                        resultmsg = "Το username δεν υπάρχει.";
                        break;
                }
            }catch(IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Verifying given credentials...",
                    "Connecting to server...");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            try {
                if(socket!=null) {
                    socket.close();
                    out.close();
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // proceed to main screen
            if(is_successful) {
                Intent main = new Intent(LoginActivity.this, MainActivity.class);
                main.putExtra("username", username);
                main.putExtra("creator", creator);
                startActivity(main);
            }
            else {
                Snackbar.make( getWindow().getDecorView().getRootView(), resultmsg , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}