package com.vsii.microservice.api_gateway.repositories;

import com.vsii.microservice.api_gateway.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :role, '%'))")
    List<Permission> findByRoleIgnoreCase(@Param("role") String role);

}
