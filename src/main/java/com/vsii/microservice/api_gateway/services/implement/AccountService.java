package com.vsii.microservice.api_gateway.services.implement;

import com.vsii.microservice.api_gateway.entities.Account;
import com.vsii.microservice.api_gateway.entities.Permission;
import com.vsii.microservice.api_gateway.entities.Role;
import com.vsii.microservice.api_gateway.repositories.AccountRepository;
import com.vsii.microservice.api_gateway.repositories.PermissionRepository;
import com.vsii.microservice.api_gateway.services.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public Map<String, List<String>> getRolesAndPermissions(String phoneNumber) {
        Account account = accountRepository.findByPhoneNumber(phoneNumber).get();
        Map<String, List<String>> rolesAndPermissions = new HashMap<>();

        List<Permission> permissions = permissionRepository.findByRoleIgnoreCase(account.getRole().getName());
        for (Permission permission : permissions) {
            rolesAndPermissions.computeIfAbsent(permission.getEndPoint(), k -> new ArrayList<>()).add(permission.getHttpMethod().name());
        }
        return rolesAndPermissions;
    }

}
