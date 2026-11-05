package com.scc.dao;

public class DatabaseInitializer {

    public static void initializer(){
        new UserDao();
        new ActivityLogDao();
        System.out.println("\n -> Database tables Verified. ");
    }
}
