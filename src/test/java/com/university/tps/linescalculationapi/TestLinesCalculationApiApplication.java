package com.university.tps.linescalculationapi;

import org.springframework.boot.SpringApplication;

public class TestLinesCalculationApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(LinesCalculationApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
