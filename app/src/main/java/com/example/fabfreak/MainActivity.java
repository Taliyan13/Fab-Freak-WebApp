package com.example.fabfreak;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    // save category names array
    public String[] category;
    // image category - event, night out, study, work
    // any category have default picture
    public int [] imageCategory={
            R.drawable.event_category,
            R.drawable.night_out_category,
            R.drawable.study_category,
            R.drawable.work_category};

    public GridView gridView;

    //-----------------------------------------
    //check user is connected and exists in the system
    //-----------------------------------------
    public boolean checkSharedPreference()
    {
        SharedPreferences sp = getSharedPreferences ("Fabric", 0) ;

        if( sp.getInt("User_id",-1) != -1)
            return true;
        return false;

    }

    //-----------------------------------------
    // clear sharePrefernce
    //-----------------------------------------
    private void deleteSheredPreference()
    {
        SharedPreferences sp = getSharedPreferences ("Fabric", 0) ;
        sp.edit().clear().apply();

    }


    //------------------------------------------------
    // Create different menus for a registered user and an unregistered user
    //------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(checkSharedPreference()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.user_menu, menu);
        }
        else
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.guest_menu,menu);
        }
        return true;
    }




    //------------------------------------------------
    //                  app menu
    // All options presented to the user are registered to the system
    // any moment user will be able to move from page to page
    //------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Intent intentMyUploadLooks = new Intent(MainActivity.this, MyUploadsActivity.class);
        Intent intentNewLookActivity = new Intent(MainActivity.this, NewLookActivity.class);
        Intent intentCategories= new Intent(MainActivity.this, MainActivity.class);
        Intent intentLogin = new Intent(MainActivity.this, SystemLoginActivity.class);
        Intent intentSignUp= new Intent(MainActivity.this, SignUpActivity.class);
        Intent intentLogout= new Intent(MainActivity.this, SystemLoginActivity.class);

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


            // Go to the "MainActivity" page
            case R.id.user_menu_categories:
                startActivity(intentCategories);
                break;

            // Go to the "NewLookActivity" page
            case R.id.user_menu_addNewLook:
                startActivity(intentNewLookActivity);
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
        setContentView(R.layout.activity_main);


        // array adapter for looks
        ArrayAdapter<String> stringAdapter;
        // category array
        category=getResources().getStringArray(R.array.categories);
        // Match category to the current adapter
        stringAdapter=new ArrayAdapter<String>(this,R.layout.per_item,category);


        gridView=findViewById(R.id.gridview);
        GridAdapter gridAdapter= new GridAdapter(this,category,imageCategory);
        gridView.setAdapter(gridAdapter);

        //OnClick listener function
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(checkSharedPreference()) {
                    String selectedCategory = category[position];
                    // match name to relevant category
                    startActivity(new Intent(getApplicationContext(), ClickOnCategoryActivity.class).putExtra("name", selectedCategory));
                }
                else
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("Login Alert");
                    builder.setMessage("Hi! in order to continue you must Login ")
                            .setCancelable(false)
                            //yes button
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    Intent intent=new Intent(MainActivity.this,SystemLoginActivity.class);
                                    startActivity(intent);

                                }
                            });
                    AlertDialog alertDialog_exit=builder.create();
                    alertDialog_exit.show();
                }
            }
        });
    }


}