package com.example.fabfreak;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class MyUploadsActivity extends AppCompatActivity {

    private Context context;
    // user info - save all user information
    private UserInfo myUser;
    private int looksNumber;
    //arrays
    private ArrayList<Look> myLooks = new ArrayList<Look>();
    private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
    private ArrayList<String> categories = new ArrayList<String>();

    private GridView myGridView;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private StorageReference storageReference = mStorageRef.getReference();

    // loading process
    private ProgressDialog loading;


    //-----------------------------------------
    // Using loading simulates the thinking of a page to process the data
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(MyUploadsActivity.this);
        loading.setMessage("Loading...");
        loading.show();
    }

    //-----------------------------------------
    // end of loading process
    //-----------------------------------------
    private void endProgressDialog() {
        loading.cancel();
    }


    //----------------------------------------
    // load from firebase user looks
    // and show them on created grid with all look information as icons and names
    //----------------------------------------
    private void loadUser() {
        // for get user id

        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        int user_id = sp.getInt("User_id", -1);

        DatabaseReference myRef = database.getReference("Users");

        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserInfo findUser;
                for (DataSnapshot u : snapshot.getChildren()) {
                    findUser = u.getValue(UserInfo.class);
                    if (findUser.u_id.equals(String.valueOf(user_id))) {
                        myUser = new UserInfo(findUser);
                        break;
                    }
                }
                loadLooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //--------------------------------------------
    // create myUploadActivity grid
    //--------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createGrid()
    {
        myGridView = findViewById(R.id.looks_gridview);
        LooksBuiltGrid gridAdapter = new LooksBuiltGrid(context, myLooks,categories ,icons);
        myGridView.setAdapter(gridAdapter);

        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //on click listener
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //create intent
                Intent intent = new Intent(getApplicationContext(), DisplaySelectedLookActivity.class);
                intent.putExtra("lookId", myLooks.get(position).id );
                intent.putExtra("category" , myLooks.get(position).cat_name);
                startActivity(intent);
            }
        });
    }


    //----------------------------------------
    // load looks icons from storage
    //----------------------------------------
    private void loadIcons(String path) {

        StorageReference islandRef = storageReference.child(path);
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                icons.add(bm);
                if (looksNumber == myLooks.size() && looksNumber == icons.size()) {
                    //loading process
                    endProgressDialog();
                    //create myUploadActivity grid
                    createGrid();
                }
            }
        });
    }

    //----------------------------------------
    // load and set correct user looks data
    //----------------------------------------

    //----------------------------------------
    // load from firebase user looks data
    // if user dont share any looks - show msg about it in grid
    // show user looks data on created grid with all look information as icons and names
    //----------------------------------------
    private void loadLooks() {
        //id size
        looksNumber = myUser.u_look_id.size();
        TextView text = findViewById(R.id.my_looks_msg);
        String msg = text.getText().toString();
        // if user dont shared looks
        if (looksNumber == 0) {
            endProgressDialog();
            text.setVisibility(View.VISIBLE);
            text.setText("You don't have any Looks yet!");
        } else {
            String categorySet[], category;
            final String[] look_id = {""};

            for (String r : myUser.u_look_id) {
                categorySet = r.split("_");
                category = categorySet[0];
                look_id[0] = categorySet[2];
                DatabaseReference myRef = database.getReference("categories").child(category).child("look_" + look_id[0]);
                myRef.orderByKey().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Look locateLook;
                        locateLook = snapshot.getValue(Look.class);
                        if (locateLook.userId.equals(myUser.u_id)) {
                            myLooks.add(new Look(locateLook));
                            categories.add(locateLook.cat_name);
                            loadIcons(locateLook.icon);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }
    }

    // main
    // on create function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_uploads);
        context = this;
        createProgressDialog();
        loadUser();
    }

    //-----------------------------------------
    //check user is connected and exists in the system
    //-----------------------------------------
    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);

        return sp.getInt("User_id", -1) != -1;

    }

    //-----------------------------------------
    // clear sharePrefernce
    //-----------------------------------------
    private void deleteSheredPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        sp.edit().clear().apply();

    }


    //------------------------------------------------
    // Create different menus for a registered user and an unregistered user
    //------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (checkSharedPreference()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_menu, menu);
        } else {
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

        final Context context = this;
        Intent intentMyLooks = new Intent(MyUploadsActivity.this, MyUploadsActivity.class);
        Intent intentNewLook = new Intent(MyUploadsActivity.this, NewLookActivity.class);
        Intent intentCategories = new Intent(MyUploadsActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(MyUploadsActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(MyUploadsActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(MyUploadsActivity.this, SystemLoginActivity.class);

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
                startActivity(intentMyLooks);
                break;

            // Go to the "NewLookActivity" page
            case R.id.user_menu_addNewLook:
                startActivity(intentNewLook);
                break;

            // Go to the "MainActivity" page
            case R.id.user_menu_categories:
                startActivity(intentCategories);
                break;

            // Go to the "SystemLoginActivity" page
            case R.id.user_menu_logout:
                startActivity(intentLogout);
                break;


        }

        return super.onOptionsItemSelected(item);
    }

}