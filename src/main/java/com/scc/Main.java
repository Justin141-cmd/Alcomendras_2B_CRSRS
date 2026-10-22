package com.scc;
import com.scc.dao.DataInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;


import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        DataInitializer.initializer();

        Scanner sc = new Scanner(System.in);
        UserDao userDao = new UserDao();

        Authentication auth = new Authentication(userDao, sc);
        auth.start();
        sc.close();
    }

}