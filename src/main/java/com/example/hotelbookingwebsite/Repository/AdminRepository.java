package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
