package com.example.fabfreak;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class DisplaySelectedLookActivity extends AppCompatActivity {

    //images in img switcher
    private ImageSwitcher imageSwitcher;
    private ImageButton btPrevious, btNext;
    private final long ONE_MEGABYTE = 5 * 1024 * 1024;
    private int currentIndex = 0;
    private int location = 0;
    private ProgressDialog loading;
    private Look thisLook;

    //firebase
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    private StorageReference storageReference = mStorageRef.getReference();

    //arrays
    private ArrayList<File> tmpFile = new ArrayList<>();
    public ArrayList<Uri> uriImages = new ArrayList<Uri>();
    private ArrayList<Bitmap> images = new ArrayList<>();

    //-----------------------------------------
    // Using loading simulates the thinking of a page to process the data
    //-----------------------------------------
    private void createProgressDialog() {
        loading = new ProgressDialog(DisplaySelectedLookActivity.this);
        loading.setMessage("Loading...");
        loading.show();
    }

    //-----------------------------------------
    // end of loading process
    //-----------------------------------------
    private void endProgressDialog() {
        loading.cancel();
    }


    //-----------------------------------------
    // find and update user look name
    //-----------------------------------------
    private void updateRecipeName() {
        TextView recName = findViewById(R.id.show_look_name);
        recName.setText(thisLook.name);
    }

    //-----------------------------------------
    // find and update user name for any look created
    //-----------------------------------------
    private void updatePublishedUser() {
        TextView byName = findViewById(R.id.By_writer);
        byName.setText("By: " + thisLook.userName);
    }



    //----------------------------------------
    // load from firebase user looks
    // and show them on created grid with all look information as icons and names
    //----------------------------------------
    private void loadLooksImages() {
        String name = "";

        int i = 0;
        location = 0;
        for (String path : thisLook.pics) {
            name = "tmp" + i;
            try {
                tmpFile.add(File.createTempFile(name, "jpg"));
                StorageReference islandRef = storageReference.child(path);
                islandRef.getFile(tmpFile.get(i++)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        uriImages.add(Uri.fromFile(tmpFile.get(location++)));
                        if (uriImages.size() == thisLook.pics.size()) {
                            setImageSwitcher();
                            endProgressDialog();
                            updateRecipeName();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //----------------------------------------
    // load from firebase user looks data for any category
    //----------------------------------------
    private void loadLook() {

        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        String category = (String) b.get("category");
        String id = (String) b.get("lookId");

        DatabaseReference myRef = database.getReference("categories").child(category).child("look_" + id);
        myRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Look locateLook;
                locateLook = snapshot.getValue(Look.class);
                thisLook = new Look(locateLook);
                loadLooksImages();
                updatePublishedUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //--------------------------------------------------------
    // images Switcher functions
    //--------------------------------------------------------
    private void setImageSwitcher() {
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.FILL_PARENT));
                return imageView;
            }
        });

        imageSwitcher.setImageURI(uriImages.get(0));

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.setInAnimation(DisplaySelectedLookActivity.this, R.anim.from_right);
                imageSwitcher.setOutAnimation(DisplaySelectedLookActivity.this, R.anim.to_left);
                --currentIndex;
                if (currentIndex < 0)
                    currentIndex = uriImages.size() - 1;
                imageSwitcher.setImageURI(uriImages.get(currentIndex));
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSwitcher.setInAnimation(DisplaySelectedLookActivity.this, R.anim.from_left);
                imageSwitcher.setOutAnimation(DisplaySelectedLookActivity.this, R.anim.to_right);
                currentIndex++;
                if (currentIndex == uriImages.size())
                    currentIndex = 0;
                imageSwitcher.setImageURI(uriImages.get(currentIndex));
            }
        });

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
        Intent intentMyRecipes = new Intent(DisplaySelectedLookActivity.this, MyUploadsActivity.class);
        Intent intentAddNewRecipe = new Intent(DisplaySelectedLookActivity.this, NewLookActivity.class);
        Intent intentCategories = new Intent(DisplaySelectedLookActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(DisplaySelectedLookActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(DisplaySelectedLookActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(DisplaySelectedLookActivity.this, SystemLoginActivity.class);

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
                startActivity(intentMyRecipes);
                break;

            // Go to the "NewLookActivity" page
            case R.id.user_menu_addNewLook:
                startActivity(intentAddNewRecipe);
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


    // create page -  load looks from DB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_selected_look);

        createProgressDialog();
        imageSwitcher = findViewById(R.id.show_look_image_switcher);
        btNext = findViewById(R.id.showLook_bt_next);
        btPrevious = findViewById(R.id.showLook_bt_previous);
        //load looks function
        loadLook();

    }
}