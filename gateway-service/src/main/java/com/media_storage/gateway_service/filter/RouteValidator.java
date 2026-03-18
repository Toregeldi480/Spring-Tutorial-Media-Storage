package com.media_storage.gateway_service.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> openApiEndpoints = List.of(
            "/auth/",
            "/eureka/"
    );

    public static final List<String> forbiddenApiEndpoints = List.of(
            "/user/changeFileCount"
    );

    public Predicate<ServerHttpRequest> isForbidden =
            request -> forbiddenApiEndpoints
                    .stream()
                    .anyMatch(uri -> request.getURI().getPath().startsWith(uri));

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));
}
