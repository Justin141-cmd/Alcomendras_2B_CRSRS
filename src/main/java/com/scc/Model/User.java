package com.scc.Model;

public record User(int user_id, String fullname, String contact,String username, String password, String role,String status, String date_created) {

    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - Status: %s ", user_id,username,role,status);

    }
}
