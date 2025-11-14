package com.example.application.repository; 

import com.example.application.model.Producto; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Con solo esto, Spring ya te da métodos como findAll(), findById(), save(), etc.
}