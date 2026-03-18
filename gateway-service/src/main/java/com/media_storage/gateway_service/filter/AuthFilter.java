package com.media_storage.gateway_service.filter;

import com.media_storage.gateway_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isForbidden.test(request)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            if (validator.isSecured.test(request)) {
                String token = extractTokenFromCookie(request);

                if (token == null) {
                    return handleUnauthorized(exchange, "Missing Authentication Token");
                }

                try {
                    Claims claims = jwtUtil.extractAllClaims(token);

                    exchange = exchange.mutate()
                            .request(r -> r
                                    .header("X-Username", claims.getSubject())
                            ).build();
                } catch (ExpiredJwtException e) {
                    return handleUnauthorized(exchange, "Token Expired");
                } catch (JwtException e) {
                    return handleUnauthorized(exchange, "Invalid Token");
                }
            }

            return chain.filter(exchange);
        });
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        String cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
        if (cookieHeader == null) return null;

        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(cookie -> cookie.startsWith("accessToken="))
                .map(cookie -> cookie.substring("accessToken=".length()))
                .findFirst()
                .orElse(null);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {

    }
}
