package com.example.fabfreak;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;


public class AnimAfterNewLookActivity extends AppCompatActivity {
    public ImageView aniView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim_after_add_new_look);

        //----------------------------------------
        // before user will move back to main activity, he will se short animation
        //----------------------------------------
        aniView = (ImageView) findViewById(R.id.logout_animation);
        //from lest to right
        ObjectAnimator mover = ObjectAnimator.ofFloat(aniView, "x", 1200f);
        mover.setDuration(3500);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mover);
        animatorSet.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(3500);
                        Intent intent = new Intent (AnimAfterNewLookActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}