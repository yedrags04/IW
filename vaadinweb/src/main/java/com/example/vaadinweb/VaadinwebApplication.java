package com.example.vaadinweb;

import com.example.vaadinweb.model.Producto;
import com.example.vaadinweb.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VaadinwebApplication {
    public static void main(String[] args) {
        SpringApplication.run(VaadinwebApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ProductoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Producto("Hamburguesa", 5.99, "Comida"));
                repo.save(new Producto("Patatas", 2.49, "Comida"));
                repo.save(new Producto("Coca-Cola", 1.99, "Bebida"));
            }
        };
    }
}
