package com.vsii.microservice.api_gateway.entities;

import com.vsii.microservice.api_gateway.enums.HttpMethodEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",nullable = false, unique = true)
    private String name; // ten quyen, ex: admin:retrieve, user:retrieve

    @Column(name = "end_point")
    private String endPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method")
    private HttpMethodEnum httpMethod;
}
