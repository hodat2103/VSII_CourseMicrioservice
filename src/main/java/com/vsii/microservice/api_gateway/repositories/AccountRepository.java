package com.vsii.microservice.api_gateway.repositories;

import com.vsii.microservice.api_gateway.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account , Integer > {
    Boolean existsByPhoneNumber(String phoneNumber);
    Optional<Account> findByPhoneNumber(String phoneNumber  );

    @Query("UPDATE Account a SET a.password = :password WHERE a.id = :accountId")
    int updatePassword(@Param("password") String password, @Param("accountId") int accountId);
}
