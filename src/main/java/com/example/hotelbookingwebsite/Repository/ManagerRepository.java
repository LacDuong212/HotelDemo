package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
}
