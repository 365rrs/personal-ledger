package com.gaoyan.personalledger;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gaoyan.personalledger.mapper")
public class PersonalLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalLedgerApplication.class, args);
    }

}
