package com.example.fabfreak;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;




public class SignUpActivity extends AppCompatActivity {

    //create a PASSWORD PATTERN that user will meet
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+!=])" +    //one or more special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{4,}" +               //at least 4 characters
                    "$");

    //users informeation
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText ver_password;


    //users array
    private ArrayList<UserInfo> users = new ArrayList<>();

    //flag to check Email is correct and not exist in system
    private boolean Emailflag;



    //----------------------------------------------------------
    // load users to firebase
    //----------------------------------------------------------
    private void loadUsers() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(UserInfo.class);
                    users.add(findUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //----------------------------------------------------------
    //                  email validation
    // Check that there is no other user with the same email address
    //----------------------------------------------------------
    private boolean validateEmail() {
        Emailflag = true;
        String emailInput = email.getText().toString().trim();
        if (emailInput.isEmpty()) {
            email.setError("you must add email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            email.setError("Please try again with valid email address");
            return false;
        } else {
            if (!users.isEmpty()) {
                for (UserInfo u : users) {
                    if (u.u_email.equals(emailInput)) {
                        email.setError("email address exist in system");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //----------------------------------------------------------
    //                  password validation
    // Check that the password complies with the established legality
    //----------------------------------------------------------
    private boolean validatePassword() {
        String passwordInput = password.getText().toString().trim();
        if (passwordInput.isEmpty()) {
            password.setError("you must add password");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            password.setError("Password must contain one or more special character and 4 characters");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }


    //----------------------------------------------------------
    //                  password validation
    // Check passwords match
    //----------------------------------------------------------
    private boolean validate_valPassword() {
        String passwordInput = password.getText().toString().trim();
        String password_var_Input = ver_password.getText().toString().trim();
        if (!(password_var_Input.equals(passwordInput))) {
            ver_password.setError("The password you entered does not match");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    //----------------------------------------------------------
    //                  username validation
    //----------------------------------------------------------
    private boolean validateUsername() {
        String usernameInput = username.getText().toString().trim();
        if (usernameInput.isEmpty()) {
            username.setError("Please add username");
            return false;
        } else if (usernameInput.length() < 2) {
            username.setError("Username must contains more than 2 characters");
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }



    //----------------------------------------------------------
    //                  confirms sign up user information
    //----------------------------------------------------------
    public void confirmInput(View v) {
        if (!validateUsername() || !validatePassword() || !validate_valPassword() || !validateEmail()) {
            if (!Emailflag)
                email.setError("exists email address.");
            return;
        }
        createUser();
    }


    //----------------------------------------------------------
    //                  user create
    // save all information in DB
    //----------------------------------------------------------
    public void createUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("Users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            // check the last user id and create new user and save it on firebase
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int last_id = (int) dataSnapshot.getChildrenCount() + 1;
                UserInfo myUser = new UserInfo(String.valueOf(last_id), username.getText().toString(), password.getText().toString(), email.getText().toString());
                DatabaseReference users = database.getReference("Users").child("user_" + last_id);
                users.child("Users").child("user_" + last_id);
                try {
                    users.setValue(myUser);
                    Toast.makeText(getApplicationContext(), "successfully sign up", Toast.LENGTH_SHORT).show();
                    finish();

                } catch (Exception e) {
                    String str = e.getMessage();
                    System.out.println(str);
                }
                Intent intent = new Intent(SignUpActivity.this, SystemLoginActivity.class);
                startActivity(intent);
                finish();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    //------------------------------------------------
    // Create different menus for a registered user and an unregistered user
    //------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //An unregistered user will not be able to be exposed to information in the app
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.guest_menu, menu);

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

        Intent intentLogin = new Intent(SignUpActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(SignUpActivity.this, SignUpActivity.class);

        switch (item.getItemId()) {

            // intent for signup guest menu
            case R.id.guest_menu_signup:
                startActivity(intentSignUp);
                break;

            // intent for login page for succsessfuly sign up to guest
            case R.id.guest_menu_login:
                startActivity(intentLogin);
                break;

        }

        return super.onOptionsItemSelected(item);
    }




    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);

        return sp.getInt("User_id", -1) != -1;

    }


    //main
    //on create function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        loadUsers();
        username = findViewById(R.id.signup_username);
        password = findViewById(R.id.signup_password);
        ver_password = findViewById(R.id.signup_password_verification);
        email = findViewById(R.id.signup_email);

        //sign up user function
        findViewById(R.id.signup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check all information are correct, create new user and save in firebase
                confirmInput(v);
            }
        });


    }

}
