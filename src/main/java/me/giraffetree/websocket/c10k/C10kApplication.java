package me.giraffetree.websocket.c10k;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author GiraffeTree
 */
@EnableScheduling
@SpringBootApplication
public class C10kApplication {

    public static void main(String[] args) {
        SpringApplication.run(C10kApplication.class, args);
    }

}
