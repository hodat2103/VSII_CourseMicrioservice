package com.vsii.microservice.api_gateway.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * lop nay cung cap truong chung la ngay tao lan dau va cap nhat lan cuoi cho cac entity extend lai.
  */

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass

public class BaseEntity {

    /**
     * Cac truong gom 'create_at' va 'update_at'.
     * duoc dinh dang theo dang mac dinh 'yyyy-MM-dd HH:mm:ss'.
     */

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
