package com.example.fabfreak;

import java.util.ArrayList;

public class UserInfo {
    public String u_id;
    public String u_name;
    public String u_pass;
    public String u_email;
    private String u_modification = "-1";
    public ArrayList<String> u_look_id = new ArrayList<String>(); ;

    public UserInfo(String id, String name, String pass, String email) {
        u_id = id;
        u_name = name;
        u_pass = pass;
        u_email = email;
    }

    public UserInfo(UserInfo user) {
        u_id = user.u_id;
        u_name = user.u_name;
        u_pass = user.u_pass;
        u_email = user.u_email;
        u_modification = user.getU_modification();
        if (!u_look_id.isEmpty())
            u_look_id.clear();
        u_look_id.addAll(user.u_look_id);

    }

    public UserInfo() {
    }

    public void setU_modification(String num) {
        this.u_modification = num;
    }

    public String getU_modification() {
        return u_modification;
    }
}
