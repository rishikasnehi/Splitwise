package com.example.splitwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This replaces "if __name__ == '__main__':" — it boots an embedded
// web server (Tomcat) instead of starting a console while-loop.
@SpringBootApplication
public class SplitwiseApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SplitwiseApiApplication.class, args);
    }
}
