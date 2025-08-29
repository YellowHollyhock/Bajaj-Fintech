package com.example.bfh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class BfhJavaApp {

    public static void main(String[] args) {
        SpringApplication.run(BfhJavaApp.class, args);
    }

    @Component
    static class Runner implements CommandLineRunner {

        private final FlowService flowService;

        @Value("${bfh.name}")
        private String name;

        @Value("${bfh.regNo}")
        private String regNo;

        @Value("${bfh.email}")
        private String email;

        @Value("${bfh.finalQuery}")
        private String finalQuery;

        Runner(FlowService flowService) {
            this.flowService = flowService;
        }

        @Override
        public void run(String... args) throws Exception {
            flowService.execute(name, regNo, email, finalQuery);
        }
    }
}
