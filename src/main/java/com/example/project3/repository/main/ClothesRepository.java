package com.example.project3.repository.main;


import com.example.project3.Entity.main.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes,Long> {
}
