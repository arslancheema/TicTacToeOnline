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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    EditText etInviteEmail;
    EditText etMyEmail;
    Button btnLogin;
    String myEmail;
    String uid;
    String playerSession;
    String mySample="X";

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
                            changeColorEditText(dataSnapshot.getValue(String.class).toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });


    }

    private void changeColorEditText(String s) {
        etInviteEmail.setBackgroundColor(Color.RED);
        etInviteEmail.setText(s);
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

        startGame(formatEmail(etInviteEmail.getText().toString()) + ":" + formatEmail(myEmail) );
        mySample = "X";
    }

    public void onAcceptClick(View view) {
        Log.d(TAG,"Invite: " + etInviteEmail.getText().toString());
        myRef.child("Users").child(formatEmail(etInviteEmail.getText().toString())).push().setValue(myEmail);

        startGame(formatEmail(  formatEmail(myEmail)  + ":" + etInviteEmail.getText().toString()));
        etInviteEmail.setBackgroundColor(Color.GREEN);
        mySample = "O";

    }

    private void startGame(String playerGameId) {

        playerSession = playerGameId;

        myRef.child("Playing").child(playerGameId).removeValue();


        // Read from the database
        myRef.child("Playing").child(playerGameId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try{
                            Player1.clear();
                            Player2.clear();
                            ActivePlayer=2;
                            HashMap<String,Object> td=(HashMap<String,Object>) dataSnapshot.getValue();
                            if (td!=null){

                                String value;

                                for(String key:td.keySet()){
                                    value=(String) td.get(key);
                                    if(!value.equals(formatEmail(myEmail)))
                                        ActivePlayer=mySample=="X"?1:2;
                                    else
                                        ActivePlayer=mySample=="X"?2:1;

                                    String[] splitID= key.split(":");
                                    AutoPlay(Integer.parseInt(splitID[1]));

                                }
                            }


                        }catch (Exception ex){}
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

    }

    public void onLoginClick(View view) {
        createAccount(etMyEmail.getText().toString(),"Arsl@n12");
    }

    public void onButtonsClick(View view) {

        if (playerSession.length()<=0)
            return;

        Button buSelected= (Button) view;
        int cellId=0;
        switch ((buSelected.getId())){

            case R.id.bu1:
                cellId=1;
                break;

            case R.id.bu2:
                cellId=2;
                break;

            case R.id.bu3:
                cellId=3;
                break;

            case R.id.bu4:
                cellId=4;
                break;

            case R.id.bu5:
                cellId=5;
                break;

            case R.id.bu6:
                cellId=6;
                break;

            case R.id.bu7:
                cellId=7;
                break;

            case R.id.bu8:
                cellId=8;
                break;

            case R.id.bu9:
                cellId=9;
                break;
        }

        myRef.child("Playing").child(playerSession).child("ChildId:"+cellId).setValue(formatEmail(myEmail));




    }


    int ActivePlayer=1; // 1- for first , 2 for second
    ArrayList<Integer> Player1= new ArrayList<Integer>();// hold player 1 data
    ArrayList<Integer> Player2= new ArrayList<Integer>();// hold player 2 data
    void PlayGame(int CellID,Button buSelected){

        Log.d("Player:",String.valueOf(CellID));

        if (ActivePlayer==1){
            buSelected.setText("X");
            buSelected.setBackgroundColor(Color.GREEN);
            Player1.add(CellID);


        }
        else if (ActivePlayer==2){
            buSelected.setText("O");
            buSelected.setBackgroundColor(Color.BLUE);
            Player2.add(CellID);


        }

        buSelected.setEnabled(false);
        CheckWiner();
    }

    void CheckWiner(){
        int Winer=-1;
        //row 1
        if (Player1.contains(1) && Player1.contains(2)  && Player1.contains(3))  {
            Winer=1 ;
        }
        if (Player2.contains(1) && Player2.contains(2)  && Player2.contains(3))  {
            Winer=2 ;
        }

        //row 2
        if (Player1.contains(4) && Player1.contains(5)  && Player1.contains(6))  {
            Winer=1 ;
        }
        if (Player2.contains(4) && Player2.contains(5)  && Player2.contains(6))  {
            Winer=2 ;
        }

        //row 3
        if (Player1.contains(7) && Player1.contains(8)  && Player1.contains(9))  {
            Winer=1 ;
        }
        if (Player2.contains(7) && Player2.contains(8)  && Player2.contains(9))  {
            Winer=2 ;
        }


        //col 1
        if (Player1.contains(1) && Player1.contains(4)  && Player1.contains(7))  {
            Winer=1 ;
        }
        if (Player2.contains(1) && Player2.contains(4)  && Player2.contains(7))  {
            Winer=2 ;
        }

        //col 2
        if (Player1.contains(2) && Player1.contains(5)  && Player1.contains(8))  {
            Winer=1 ;
        }
        if (Player2.contains(2) && Player2.contains(5)  && Player2.contains(8))  {
            Winer=2 ;
        }


        //col 3
        if (Player1.contains(3) && Player1.contains(6)  && Player1.contains(9))  {
            Winer=1 ;
        }
        if (Player2.contains(3) && Player2.contains(6)  && Player2.contains(9))  {
            Winer=2 ;
        }


        if ( Winer !=-1){
            // We have winer

            if (Winer==1){
                Toast.makeText(this,"Player 1 is winner",Toast.LENGTH_LONG).show();
            }

            if (Winer==2){
                Toast.makeText(this,"Player 2 is winner",Toast.LENGTH_LONG).show();
            }

        }

    }

    void AutoPlay(int CellID){

        Button buSelected;
        switch (CellID){

            case 1 :
                buSelected=(Button) findViewById(R.id.bu1);
                break;

            case 2:
                buSelected=(Button) findViewById(R.id.bu2);
                break;

            case 3:
                buSelected=(Button) findViewById(R.id.bu3);
                break;

            case 4:
                buSelected=(Button) findViewById(R.id.bu4);
                break;

            case 5:
                buSelected=(Button) findViewById(R.id.bu5);
                break;

            case 6:
                buSelected=(Button) findViewById(R.id.bu6);
                break;

            case 7:
                buSelected=(Button) findViewById(R.id.bu7);
                break;

            case 8:
                buSelected=(Button) findViewById(R.id.bu8);
                break;

            case 9:
                buSelected=(Button) findViewById(R.id.bu9);
                break;
            default:
                buSelected=(Button) findViewById(R.id.bu1);
                break;

        }
        PlayGame(CellID, buSelected);
    }



}

