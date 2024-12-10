package com.vsii.microservice.api_gateway.filter;

import com.vsii.microservice.api_gateway.services.IAccountService;
import com.vsii.microservice.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${api.prefix}")
    private String apiPrefix;

    private final JwtUtil jwtUtil;
    private final WebClient.Builder webClientBuilder;

    public List<String> publicEndpoints() {
        return Arrays.asList(
                String.format("%s/auth-service/login", apiPrefix),
                String.format("%s/course-service/health-check", apiPrefix),
                "/v2/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/swagger-ui/**",
                "/webjars/**"
        );
    }
    private final IAccountService accountService;
    private String phoneNumber = "";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter...");
        ServerHttpRequest request = exchange.getRequest();
        log.info("Rewritten Request URI: " + request.getURI().getPath());
        if (isPublicEndpoint(request.getURI().getPath(),publicEndpoints())) {
            return chain.filter(exchange);
        }
        if (isAuthorizationMissing(request))
            return unauthenticated(exchange.getResponse());
//        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(request.getHeaders()))
            return unauthenticated(exchange.getResponse());

//        String token = request.getHeaders().getOrEmpty("Authorization").get(0);
        String token = jwtUtil.extractToken(request);
//        log.info("token: {}",token);

        try {
            if (!jwtUtil.isTokenExpired(token)) {
                String phoneNumber = jwtUtil.extractPhoneNumber(token);
//                log.info("Phone number: {}", phoneNumber);
                Map<String, List<String>> rolesAndPermissions = accountService.getRolesAndPermissions(phoneNumber);
                return authorizeRequest(rolesAndPermissions, exchange, chain);
            } else {
                log.info("Token invalid");
                return unauthenticated(exchange.getResponse());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    private Mono<Void> authorizeRequest(Map<String, List<String>> rolesAndPermissions, ServerWebExchange
            exchange, GatewayFilterChain chain) {
        String serviceId = getServiceIdFromRequest(exchange.getRequest());
        String httpMethod = String.valueOf(exchange.getRequest().getMethod());
        if (hasAccess(serviceId, httpMethod, rolesAndPermissions)) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean hasAccess(String serviceId, String httpMethod, Map<String, List<String>> rolesAndPermissions) {
        for (Map.Entry<String, List<String>> entry : rolesAndPermissions.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.contains(serviceId)) {
                for (String httpMethodRequest : entry.getValue()){
                    if(httpMethodRequest.equals(httpMethod)){
                        return true;
                    }
                }

                return true;
            }
        }
        return false;
    }

    private String convertToRegex(String pattern) {
        return pattern.replace("**", ".*");
    }

    private String getServiceIdFromRequest(ServerHttpRequest request) {
        String uri = request.getURI().getPath();
        log.info(("Request URI: " + uri));
        String[] parts = uri.split("/");
        for (String part : parts) {
            log.info("URI Part: " + part);
        }
        return parts.length > 3 ? parts[3] : null;
    }


    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        String body = "unauthenticated";
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
    private boolean isPublicEndpoint(String path, List<String> publicEndpoints) {
        return publicEndpoints.stream().anyMatch(endpoint ->
                path.startsWith(endpoint));
    }



    /**
     * Handles authentication errors by setting UNAUTHORIZED status.
     *
     * @param exchange     The current server web exchange.
     * @param errorMessage The error message to log.
     * @return Mono<Void> representing the completion of error handling.
     */
    private Mono<Void> handleAuthenticationError(ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Log the error
        log.error(errorMessage);

        return response.setComplete();
    }

    /**
     * Checks if Authorization header is missing.
     *
     * @param request The incoming server HTTP request.
     * @return boolean indicating whether Authorization header is missing.
     */
    private boolean isAuthorizationMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }




}
