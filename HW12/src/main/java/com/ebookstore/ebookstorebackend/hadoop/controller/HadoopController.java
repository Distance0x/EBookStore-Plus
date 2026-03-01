package com.ebookstore.ebookstorebackend.hadoop.controller;

import com.ebookstore.ebookstorebackend.hadoop.service.HadoopJobService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hadoop")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class HadoopController {

    @Autowired
    private HadoopJobService hadoopJobService;

    @PostMapping("/analyze-keywords")
    public ResponseEntity<?> analyzeKeywords() {
        try {
            Map<String, Integer> result = hadoopJobService.analyzeKeywords();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
