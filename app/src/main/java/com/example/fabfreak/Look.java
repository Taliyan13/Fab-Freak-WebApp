package com.example.fabfreak;

import java.util.ArrayList;

public class Look
{
    public String id;
    public String cat_name;
    public String name;
    public String icon;
    public ArrayList<String> pics  = new ArrayList<String>();
    public String userId;
    public String userName;

    // in DB dave as string for any info
    // pics will save with pic id
    public Look (String id,String cat_name, String name,String icon, ArrayList<String> pics, String userId , String userName)
    {
        this.id = id;
        this.cat_name = cat_name;
        this.name = name;
        this.icon = icon;
        this.pics.addAll(pics);
        this.userId = userId;
        this.userName = userName;
    }

    public Look (Look look)
    {
        this.id = look.id;
        this.cat_name = look.cat_name;
        this.name = look.name;
        this.icon = look.icon;
        this.pics.addAll(look.pics);
        this.userId = look.userId;
        this.userName = look.userName;
    }
    public Look()
    {

    }
}