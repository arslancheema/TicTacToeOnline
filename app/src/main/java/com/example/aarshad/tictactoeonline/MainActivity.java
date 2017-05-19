package com.example.aarshad.tictactoeonline;

import android.graphics.Color;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    EditText etInviteEmail;
    EditText etMyEmail;
    Button btnLogin;
    String myEmail;
    String uid;

    // Firebase
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInviteEmail = (EditText) findViewById(R.id.etInviteEmail);
        etMyEmail = (EditText) findViewById(R.id.etMyEmail);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    myEmail = user.getEmail();
                    btnLogin.setEnabled(false);
                    etMyEmail.setText(myEmail);

                    incomingRequests();
                    uid = user.getUid();



                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


    }

    private void incomingRequests() {

        myRef.child("Users").child(formatEmail(myEmail)).child("Request_From")
        //myRef.child("Users").child(formatEmail(myEmail)).child("Request")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue(String.class) != null) {
                    Toast.makeText(getApplicationContext(), "New Request: " + dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
                    changeColorEditText();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    private void changeColorEditText() {
        etInviteEmail.setBackgroundColor(Color.RED);
    }

    private String formatEmail(String email){
        String [] split = email.split("@");
        return split[0];
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            myEmail = user.getEmail();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
        // [END create_user_with_email]
    }

    public void onInviteClick(View view) {

        //
        myRef.child("Users").child(formatEmail(etInviteEmail.getText().toString())).child("Request_From").setValue(myEmail);


        myRef.child("Users").child(formatEmail(etInviteEmail.getText().toString())).push().setValue(myEmail);
    }

    public void onAcceptClick(View view) {
        Log.d(TAG,"Invite: " + etInviteEmail.getText().toString());
    }

    public void onLoginClick(View view) {
        createAccount(etMyEmail.getText().toString(),"Arsl@n12");
    }

    public void onButtonsClick(View view) {
    }

}
