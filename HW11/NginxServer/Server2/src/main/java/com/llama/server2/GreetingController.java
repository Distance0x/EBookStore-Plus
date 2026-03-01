package com.llama.server2;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600)
@RestController
public class GreetingController {

    @GetMapping("/")
    public String getHome() {
        return "Let' start! Server Two";
    }
}