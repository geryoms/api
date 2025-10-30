package com.myfinance.api.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import com.myfinance.api.model.User;

public abstract class BaseController {

    protected User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}