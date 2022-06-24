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

public class ClickOnCategoryActivity extends AppCompatActivity {

    private TextView categoryName;
    private TextView looks_amount;
    private int looksNumber;
    private String category;

    //grid
    private GridView myGridView;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;
    private Context context;

    // data base
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private final StorageReference storageReference = mStorageRef.getReference();

    //arrays
    private ArrayList<Bitmap> icons = new ArrayList<Bitmap>();
    private ArrayList<Look> looks = new ArrayList<Look>();
    private ArrayList<String> usersNames = new ArrayList<String>();

    //loading process
    private ProgressDialog loading;


    //----------------------------------------
    // when user click on some category in main activity, he will move to dynamic activity
    // that create grid for click listener
    // and show all category information (looks users has shared)
    //----------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createGrid()
    {
        myGridView = findViewById(R.id.looks_gridview);
        // take look users information as names and icons
        LooksBuiltGrid grid_adapter = new LooksBuiltGrid(context, looks,usersNames, icons);
        myGridView.setAdapter(grid_adapter);
        myGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //create intent that take user look id and category he shared this look
                Intent intent = new Intent(getApplicationContext(), DisplaySelectedLookActivity.class);
                intent.putExtra("lookId", looks.get(position).id );
                intent.putExtra("category" , looks.get(position).cat_name);
                startActivity(intent);
            }
        });
    }


    //----------------------------------------
    // load from firebase storage the looks icons, and set them on grid
    //----------------------------------------
    private void loadIcons(String path) {

        StorageReference islandRef = storageReference.child(path);
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                icons.add(bm);
                endProgressDialog();
                if (looksNumber == looks.size() && looksNumber == icons.size() && looksNumber != 0)
                    createGrid();
            }
        });
    }

    //----------------------------------------
    // load from firebase user looks
    // and show them on created grid with all look information as icons and names
    //----------------------------------------
    private void loadLooks() {

        DatabaseReference myRef = database.getReference("categories").child(category);
        myRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                looksNumber = (int) snapshot.getChildrenCount();
                // if user doesn't have any looks save in firebase return text on created grid
                // that there is no looks yet
                if(looksNumber == 0)
                {
                    endProgressDialog();
                    looks_amount.setVisibility(View.VISIBLE);
                    looks_amount.setText("No locks have been uploaded yet.");
                }
                else
                {
                    Look locateLook;
                    for (DataSnapshot u : snapshot.getChildren()) {
                        locateLook = u.getValue(Look.class);
                        looks.add(new Look(locateLook));
                        usersNames.add(locateLook.userName);
                        loadIcons(locateLook.icon);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                endProgressDialog();

            }});
    }

    //-----------------------------------------
    // Using loading simulates the thinking of a page to process the data
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(ClickOnCategoryActivity.this);
        loading.setMessage("Loading...");
        loading.show();
    }

    //-----------------------------------------
    // end of loading process
    //-----------------------------------------
    private void endProgressDialog() {
        loading.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_on_category);
        // loading process
        createProgressDialog();
        context = this;
        categoryName= findViewById(R.id.show_category_name);
        //  load category name
        Intent intent=getIntent();
        if (intent.getExtras() != null){
            category=intent.getStringExtra("name");
            categoryName.setText(category);
            // show looks category
            looks_amount = findViewById(R.id.looks_amount);
            loadLooks();
        }
    }

    //-----------------------------------------
    //check user is connected and exists in the system
    //-----------------------------------------
    public boolean checkSharedPreference() {
        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        //return userid
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
        //A registered user will be able to view the information in the app
        if (checkSharedPreference()) {
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

        final Context context = this;
        Intent intentMyUploadLooks = new Intent(ClickOnCategoryActivity.this, MyUploadsActivity.class);
        Intent intentNewLook = new Intent(ClickOnCategoryActivity.this, NewLookActivity.class);
        Intent intentCategories = new Intent(ClickOnCategoryActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(ClickOnCategoryActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(ClickOnCategoryActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(ClickOnCategoryActivity.this, SystemLoginActivity.class);

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