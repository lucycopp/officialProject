package com.example.android.fyp;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class SignUp extends AppCompatActivity {

    Button signUpButton;
    EditText emailInput, password1Input, password2input;
    RadioButton operatorOption, tourGuideOption;
    private FirebaseAuth mAuth;
    boolean successfulFirebase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        signUpButton = (Button) findViewById(R.id.signUpNewUserButton);
        emailInput = (EditText) findViewById(R.id.emailAddressSignUp);
        password1Input = (EditText) findViewById(R.id.passwordSignUp1);
        password2input = (EditText) findViewById(R.id.passwordSignUp2);
        operatorOption = (RadioButton) findViewById(R.id.operatorRadio);
        tourGuideOption = (RadioButton) findViewById(R.id.tourGuideRadio);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if (isInputtedDataValid(emailInput.getText().toString(), password1Input.getText().toString(), password2input.getText().toString()) == true) {
                    if (operatorOption.isChecked()) {
                        createNewUserFirebase(emailInput.getText().toString(), password1Input.getText().toString(), false);
                    } else if (tourGuideOption.isChecked()) {
                        createNewUserFirebase(emailInput.getText().toString(), password1Input.getText().toString(), true);
                    } else{
                        Toast.makeText(getApplicationContext(), "Select an account type", Toast.LENGTH_LONG).show();
                    }
                    }
                }
        });
    }


    private void createNewUserFirebase(String email, String password, final Boolean guide){
        mAuth.createUserWithEmailAndPassword(email, password) //create a new fire base user
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser(); //get the new user
                            successfulFirebase = true;
                            new AddUserToSQLDatabase(guide).execute(emailInput.getText().toString()); //add user to the database
                            password1Input.setText("");
                            emailInput.setText("");
                            password2input.setText("");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            successfulFirebase = false;
                        }

                    }
                });
    }

    private class AddUserToSQLDatabase extends AsyncTask<String, Void, String> {
        int guide = 0;
        private AddUserToSQLDatabase(boolean mGuide){
            if (mGuide)
                guide = 1;
        }
        @Override
        protected String doInBackground(String... args) {
            String name = args[0];
            String jo = null;
            URL url = JSONUtils.makeURL("http://lcgetdata.azurewebsites.net/addUser.php?email=" + args[0] + "&guide=" + guide); //URL to add user
            try {
                jo = JSONUtils.makeHTTPRequest(url); //get response from HTTP Request
            } catch (IOException e){
                Log.i("AddUser", e.toString()); //log exception
                Toast.makeText(getApplicationContext(), "ERROR: " + e.toString(), Toast.LENGTH_LONG).show(); //display exception
            }
            return jo;
            //connect to the web app
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<html>","");
            Toast.makeText(getApplicationContext(), "User Added",
                    Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public final static boolean isEmailValid(CharSequence inputEmail) {
        if (inputEmail == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches();
        }
    }

    public boolean isInputtedDataValid(String email, String password1, String password2){
        Boolean valid = true;
        if (!isEmailValid(email)) {
            Toast.makeText(getApplicationContext(), "Invalid Email",
                    Toast.LENGTH_LONG).show();
            valid = false;
        }
        if(!password1.equals(password2)) {
            Toast.makeText(getApplicationContext(), "Passwords do not match " + password1 + " " + password2,
                    Toast.LENGTH_LONG).show();
            valid = false;
        }
        return valid;
    }

}
