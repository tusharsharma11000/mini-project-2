package com.example.CRUD;

import com.example.CRUD.entity.User;
import com.example.CRUD.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class Controller {

    @Autowired
    UserService userService;

    @GetMapping("/ping")
    public String ping(){
        return "Backend is running";
    }

    @GetMapping("/json")
    public Map<String, Object> json(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "Social Media API Running");
        map.put("status", "UP");
        return map;
    }
}
