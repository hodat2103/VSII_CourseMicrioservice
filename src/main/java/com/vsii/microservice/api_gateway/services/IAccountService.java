package com.vsii.microservice.api_gateway.services;

import java.util.List;
import java.util.Map;

public interface IAccountService {
    public Map<String, List<String>> getRolesAndPermissions(String phoneNumber);
}
