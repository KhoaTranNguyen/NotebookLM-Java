package com.khoa.notebooklm.desktop.controller;

import com.khoa.notebooklm.desktop.model.dao.UserDao;

public class AuthController {

    private final UserDao userDao = new UserDao();

    public boolean login(String username, String password) {
        return userDao.checkCredentials(username, password);
    }
}
