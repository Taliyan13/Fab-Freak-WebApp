package com.example.fabfreak;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;


public class SystemLoginActivity extends AppCompatActivity {

    private UserInfo user;
    public EditText password;
    public EditText email;

    private ArrayList<UserInfo> users = new ArrayList<>();


    //-----------------------------------------
    //check user is connected and exists in the system
    //-----------------------------------------
    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);

        if (sp.getInt("User_id", -1) != -1)
            return true;
        return false;
    }

    //-----------------------------------------
    // clear sharedPrefernce
    //-----------------------------------------
    private void deleteSheredPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        sp.edit().clear().apply();
    }



    //------------------------------------------------
    // synchronize between firebase and app
    //------------------------------------------------
    private void loadUserData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(UserInfo.class);
                    users.add(new UserInfo(findUser));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //------------------------------------------------
    //              login validation
    //check email address and password
    //------------------------------------------------

    // email validation
    private boolean validateEmail() {

        String emailInput = email.getText().toString().trim();
        if (emailInput.isEmpty()) {
            email.setError("please add email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please enter a valid email address");
            return false;
        } else {
            for (UserInfo value : users) {
                if (value.u_email.equals(email.getText().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    //password validation
    private boolean validatePassword() {
        String passwordInput = password.getText().toString();
        if (passwordInput.isEmpty()) {
            password.setError("please add password");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }


    public void confirmInput(View v) {
        if (!validateEmail() || !validatePassword())
            Toast.makeText(getApplicationContext(), "illegals inputs ", Toast.LENGTH_SHORT).show();
        else {
            login(v);
        }
    }


    //------------------------------------------------
    // login to system function
    // need to check user name exists in system with the current password
    // if yes- login to system
    // if no- alert about it
    //------------------------------------------------
    private void login(View v) {

        for (UserInfo value : users) {
            if (value.u_email.equals(email.getText().toString()) && value.u_pass.equals(password.getText().toString())) {
                user = new UserInfo(value);
                break;
            }
        }
        if (user != null) {
            createSharedPreferences();
            Toast.makeText(getApplicationContext(), "login was successfully!", Toast.LENGTH_SHORT).show();

            // success login move to main activity
            Intent intent = new Intent(SystemLoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), "Error! wrong email or password", Toast.LENGTH_SHORT).show();

    }



    // save user id and user name in firebase
    public void createSharedPreferences() {
        // create user shared preference
        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        SharedPreferences.Editor sedt = sp.edit();

        //set verification mode and update user firebase
        if (user != null) {
            user.setU_modification("-1");
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference("Users").child("user_" + user.u_id);
            myRef.child("Users").child("user_" + user.u_id);
            myRef.setValue(user);

            sedt.putString("User_Name", this.user.u_name);
            sedt.putInt("User_id", Integer.parseInt(this.user.u_id));
            sedt.apply();
        }

    }




    //------------------------------------------------
    // Create different menus for a registered user and an unregistered user
    //------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (checkSharedPreference()) {
            //A registered user will be able to view the information in the app
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_menu, menu);
        } else {
            //An unregistered user will not be able to be exposed to information in the app
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.guest_menu, menu);
        }
        return true;
    }

    //------------------------------------------------
    //                  app menu
    // All options presented to the user are registered to the system
    // any moment user will be able to move from page to page
    //------------------------------------------------
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intentMyUploadLooks = new Intent(SystemLoginActivity.this, MyUploadsActivity.class);
        Intent intentNewLookActivity = new Intent(SystemLoginActivity.this, NewLookActivity.class);
        Intent intentCategories = new Intent(SystemLoginActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(SystemLoginActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(SystemLoginActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(SystemLoginActivity.this, SystemLoginActivity.class);

        switch (item.getItemId()) {

            //------------------------------------------------
            // options for unregistered users
            //------------------------------------------------

            // Go to the "SignUpActivity" page
            case R.id.guest_menu_signup:
                startActivity(intentSignUp);
                break;

            // Go to the "SystemLoginActivity" page
            case R.id.guest_menu_login:
                startActivity(intentLogin);
                break;


            //------------------------------------------------
            // options for registered users
            //------------------------------------------------

            // Go to the "MyUploadsActivity" page
            case R.id.user_menu_myLooks:
                startActivity(intentMyUploadLooks);
                break;

            // Go to the "NewLookActivity" page
            case R.id.user_menu_addNewLook:
                startActivity(intentNewLookActivity);
                break;

            // Go to the "MainActivity" page
            case R.id.user_menu_categories:
                startActivity(intentCategories);
                break;

            // Go to the "SystemLoginActivity" page
            case R.id.user_menu_logout:
                deleteSheredPreference();
                startActivity(intentLogout);
                break;


            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        return super.onOptionsItemSelected(item);
    }



    //on create function
    //main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systemlogin);
        loadUserData();
        //load user email and password
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        // login button
        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput(v);
            }
        });

        //sign up button
        findViewById(R.id.askToSignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SystemLoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

}
