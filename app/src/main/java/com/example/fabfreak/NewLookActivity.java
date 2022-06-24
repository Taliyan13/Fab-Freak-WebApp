package com.example.fabfreak;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;


public class NewLookActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int GALLERY_REQUEST_CODE = 123;
    private ImageView iconView;
    private Bitmap icon;
    public Spinner category_spinner;
    public Button btnPick;

    private EditText look_name;
    private String category = "";
    private int look_id;
    private UserInfo myUser;
    private int user_id;

    // image grid
    public Button pickImageBtn;
    public ImageButton btPrevious, btNext;
    public ImageSwitcher imageSwitcher;
    public ClipData clipData;

    //arrays
    public ArrayList<Uri> uri = new ArrayList<Uri>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    //index for image switcher
    private int currentIndex = 0;



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


    //---------------------------------------------------
    // create new look
    // upload images and icon image
    //set look name
    // and choose the relevant category
    //---------------------------------------------------
    private void upload() {

        // progress bar
        final ProgressBar p = findViewById(R.id.progressbar);
        p.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        // save images in storage with name+id
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ArrayList<String> images_ids = new ArrayList<String>();
        StorageReference imageRef;
        String image_id;
        int i = 0;
        byte[] b;

        //convert to bitmap for storage saving
        for (Bitmap img : bitmaps) {
            image_id = category + "/id_" + String.valueOf(look_id) + "/userid_" + String.valueOf(user_id) + "/images/" + i + ".jpeg";
            images_ids.add(image_id);
            img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageRef = mStorageRef.child(image_id);

            b = stream.toByteArray();
            imageRef.putBytes(b);
            stream.reset();
            i++;

        }

        // Icon
        String icon_id = category + "/id_" + String.valueOf(look_id) + "/userid_" + String.valueOf(user_id) + "/icon.jpeg";
        icon.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        b = stream.toByteArray();
        StorageReference iconRef = mStorageRef.child(icon_id);
        iconRef.putBytes(b);

        // create new look and save in DB
        String name = look_name.getText().toString();
        Look myLook = new Look(String.valueOf(look_id), category, name, icon_id, images_ids, String.valueOf(user_id), myUser.u_name);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        System.out.println("here");
        //upload user looks
        DatabaseReference userRef = database.getReference("Users");
        DatabaseReference users = database.getReference("Users").child("user_" + user_id);
        users.child("Users").child("user_" + user_id);
        myUser.u_look_id.add(category + "_id_" + look_id);
        System.out.println("my user");
        System.out.println(myUser);
        users.setValue(myUser);

        //upload to firebase
        DatabaseReference ref = database.getReference("categories").child(category).child("look_" + look_id);
        ref.child("categories").child(category).child("look_" + look_id);

        ref.setValue(myLook).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                p.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(NewLookActivity.this, "New look uploaded ", Toast.LENGTH_SHORT).show();
                Intent intentMyLooks = new Intent(NewLookActivity.this, AnimAfterNewLookActivity.class);
                startActivity(intentMyLooks);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(NewLookActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //----------------------------------------
    //verify that a category does exist
    //----------------------------------------
    private boolean categoryValidation() {
        if (category.equals("")) {
            final Context context = this;
            // user msg
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Category Error!");
            alertDialogBuilder.setMessage("You must select category for  your look!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }


    //----------------------------------------
    // verify that a icon does exist
    //----------------------------------------
    private boolean iconValidation() {
        if (iconView.getDrawable() == null) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Icon Error!");
            alertDialogBuilder.setMessage("You must select icon for  your look!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // verify that a look id does exist
    //----------------------------------------
    private boolean idValidation() {
        return look_id != -1;
    }

    //----------------------------------------
    // verify that a look name does exist
    //----------------------------------------
    private boolean lookNameValidation() {
        if (look_name.getText().toString().equals("")) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Name Error!");
            alertDialogBuilder.setMessage("You must insert name for your look").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //----------------------------------------
    // verify that a look images does exist
    //----------------------------------------
    private boolean imagesValidation() {
        if (uri.isEmpty() && bitmaps.isEmpty()) {
            final Context context = this;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("Upload Error!");
            alertDialogBuilder.setMessage("You must upload image for your look!").setCancelable(true).setPositiveButton("Ok", new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return false;
        }
        return true;
    }

    //Check that indeed all the details were uploaded as requested
    private boolean uploadValidation() {
        return lookNameValidation()  && categoryValidation() && idValidation() && iconValidation() && imagesValidation();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();

        // set look id from his category - last id +1
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("categories").child(category);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                look_id = (int) dataSnapshot.getChildrenCount() + 1;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        category = "";
        look_id = -1;
    }



    //----------------------------------------
    // load from firebase storage the user data information
    //----------------------------------------
    private void loadUser() {
        // for get user id
        SharedPreferences sp = getSharedPreferences("Fabric", 0);
        user_id = sp.getInt("User_id", -1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // main
    // onCreate function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_look);

        loadUser();
        look_name = findViewById(R.id.look_name);

        //for categories  spinner
        category_spinner = findViewById(R.id.category_spinner_id);
        //adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(NewLookActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.categories));
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(spinnerAdapter);
        category_spinner.setOnItemSelectedListener(this);

        iconView = findViewById(R.id.myIconView);

        btnPick = findViewById(R.id.btnPickImage);
        btnPick.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });



        //----------------------------------------
        // upload button
        //check all information correct
        //----------------------------------------
        Button upload = findViewById(R.id.upload_look);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadValidation()) {
                    upload();

                }
            }
        });


        //----------------------------------------
        // upload look images button
        //----------------------------------------
        pickImageBtn = findViewById(R.id.pickImageBtn);
        pickImageBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                if (!bitmaps.isEmpty())
                    bitmaps.clear();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

    }

    //--------------------------------------------------------
    // images Switcher functions
    //--------------------------------------------------------
    private void setImageSwitcher() {
        //image switcher
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

        imageSwitcher.setImageURI(uri.get(0));

        btPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.setInAnimation(NewLookActivity.this, R.anim.from_right);
                imageSwitcher.setOutAnimation(NewLookActivity.this, R.anim.to_left);
                --currentIndex;
                if (currentIndex < 0)
                    currentIndex = uri.size() - 1;
                imageSwitcher.setImageURI(uri.get(currentIndex));
            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageSwitcher.setInAnimation(NewLookActivity.this, R.anim.from_left);
                imageSwitcher.setOutAnimation(NewLookActivity.this, R.anim.to_right);
                currentIndex++;
                if (currentIndex == uri.size())
                    currentIndex = 0;
                imageSwitcher.setImageURI(uri.get(currentIndex));
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            //set for icon
            if (requestCode == 0 && resultCode == RESULT_OK) {
                icon = (Bitmap) data.getExtras().get("data");
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageBitmap(icon);
            } else if (requestCode == 1 && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    icon = bitmap;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageBitmap(icon);
            }else {

                // set for images from gallery
                if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
                    imageSwitcher = findViewById(R.id.image_switcher);
                    imageSwitcher.setVisibility(View.VISIBLE);
                    btNext = findViewById(R.id.showLook_bt_next);
                    btNext.setVisibility(View.VISIBLE);
                    btPrevious = findViewById(R.id.showLook_bt_previous);
                    btPrevious.setVisibility(View.VISIBLE);
                    //create array of images
                    clipData = data.getClipData();
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            uri.add(imageUri);
                            try {
                                InputStream is = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                //add the image to array
                                bitmaps.add(bitmap);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Uri imageUri = data.getData();
                        uri.add(imageUri);
                        try {
                            InputStream is = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            bitmaps.add(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    setImageSwitcher();
                }
            }
        }
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
        Intent intentMyUploudLooks = new Intent(NewLookActivity.this, MyUploadsActivity.class);
        Intent intentNewLook = new Intent(NewLookActivity.this, NewLookActivity.class);
        Intent intentCategories = new Intent(NewLookActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(NewLookActivity.this, SystemLoginActivity.class);
        Intent intentSignUp = new Intent(NewLookActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(NewLookActivity.this, SystemLoginActivity.class);

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
                startActivity(intentMyUploudLooks);
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
