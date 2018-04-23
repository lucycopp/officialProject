package com.example.android.fyp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class LogIn extends AppCompatActivity {
    Button logInButton;
    EditText email, password;
    private FirebaseAuth mAuth;
    String LOG_TAG = "LOG IN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        logInButton = (Button) findViewById(R.id.logInButton);
        email = (EditText) findViewById(R.id.emailAddressLogIn);
        password = (EditText) findViewById(R.id.passwordLogIn);
        mAuth = FirebaseAuth.getInstance();

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                signInUser(email.getText().toString(), password.getText().toString());
            }
        });
    }

    private class getUserFromDatabase extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            String name = args[0];
            String result = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/searchUser.php?email=" + args[0]); //make ur;
            try {
                result = JSONUtils.makeHTTPRequest(url); //get response from httprequest
            } catch (IOException e){
                Log.e("SIGN IN", "HTTP REQUEST: " + e.toString()); //log exception
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            if (result == null || result.trim() == "") {  return;} //if no response return
            else {
                try {
                    JSONArray read = new JSONArray(result); //get result
                    JSONObject json = read.getJSONObject(0); //get object
                    int ID = json.getInt("User ID"); //get user ID
                    String email = json.getString("Email"); //get email
                    Boolean guide = json.getBoolean("Guide"); //get whether guide or not


                    if (guide == true){ //if guide
                        Intent i = new Intent(LogIn.this, GuideScreen.class);
                        i.putExtra("ID", ID);
                        i.putExtra("Email", email); //send user data to new activity
                        startActivity(i); //open guide screen activity
                    }
                    else if (guide == false){
                        Intent i = new Intent(LogIn.this, OperatorScreen.class);
                        i.putExtra("ID", ID);
                        i.putExtra("Email", email); //send user data to new activity
                        startActivity(i); //open operator screen activity
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }

        }
    }

    public void signInUser(final String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)  //attempt to sign in firebase authentication
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser(); //get the user
                            Toast.makeText(getApplicationContext(), "Signed in",
                                    Toast.LENGTH_LONG).show();
                            new getUserFromDatabase().execute(email);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Unable to sign in",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
