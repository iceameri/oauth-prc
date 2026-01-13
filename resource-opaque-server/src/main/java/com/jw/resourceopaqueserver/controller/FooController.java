package com.jw.resourceopaqueserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FooController {

    @GetMapping("/foo")
    public ResponseEntity<Object> foo() {
        log.info("foo check");
        return ResponseEntity.ok("foo check");
    }
}