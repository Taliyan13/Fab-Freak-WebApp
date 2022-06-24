package com.example.fabfreak;


import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import java.util.ArrayList;


/*
BaseAdapter is a common base class of a general implementation of an Adapter that can be used in ListView,
GridView, Spinner etc. Whenever you need a customized list in a ListView or customized grids in a GridView
you create your own adapter and extend base adapter in that.
*/

public class LooksBuiltGrid extends BaseAdapter {

    private Context context;
    // looks name array
    private ArrayList<String> looksName = new ArrayList<String>();
    // for category
    private final ArrayList<String> categoryOrUser = new ArrayList<String>();
    private ArrayList<Bitmap> categoryIcon = new ArrayList<Bitmap>();
    //layout
    private LayoutInflater layoutInflater;



    @RequiresApi(api = Build.VERSION_CODES.M)
    public LooksBuiltGrid(Context context, ArrayList<Look> looks,ArrayList<String> names, ArrayList<Bitmap> categoryIcon){
        this.context = context;

        this.categoryOrUser.addAll(names);
        for (Look r : looks)
        {
            this.looksName.add(r.name);
        }
        this.categoryIcon.addAll(categoryIcon);
        this.layoutInflater=(LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return looksName.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    //------------------------------------------------
    //                          grid view
    // for any look - look name, images, icon , category and created user name
    //------------------------------------------------

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = layoutInflater.inflate(R.layout.grid_items,viewGroup, false);
        }
        ImageView imageView = view.findViewById(R.id.imageview);
        TextView textView = view.findViewById(R.id.textview);
        TextView textView1=view.findViewById(R.id.textview1);
        imageView.setImageBitmap(categoryIcon.get(position));
        textView.setText(categoryOrUser.get(position));
        textView1.setText(looksName.get(position));

        return view;
    }
}