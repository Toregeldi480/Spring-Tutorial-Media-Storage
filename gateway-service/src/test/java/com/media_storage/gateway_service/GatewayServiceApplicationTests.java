package com.media_storage.gateway_service;

import com.media_storage.gateway_service.entity.UserDto;
import com.media_storage.gateway_service.util.SoutHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
class GatewayServiceApplicationTests {
	SoutHelper sout = new SoutHelper();
	RestTestClient restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:8080").build();

	@Test
	void contextLoads() {
		// fallback
		restTestClient.get().uri("/user/me")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		restTestClient.get().uri("/user/all")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		restTestClient.get().uri("/user/changeFileCount")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.FORBIDDEN);

		restTestClient.get().uri("/file/all")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		restTestClient.get().uri("/file/get")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		restTestClient.get().uri("/file/upload")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
		restTestClient.get().uri("/file/delete")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);

		restTestClient.post().uri("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"username\": \"admin\", \"password\": \"admin123\"}")
				.exchange()
				.expectStatus().isOk();
		restTestClient.post().uri("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"username\": \"admin\", \"password\": \"admin123\"}")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.CONFLICT);

		// auth
		EntityExchangeResult<UserDto> user = restTestClient.post().uri("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"username\": \"admin\", \"password\": \"admin123\"}")
				.exchange()
				.expectStatus().isOk()
				.expectBody(UserDto.class)
				.returnResult();
		UserDto userBody = user.getResponseBody();
		Assertions.assertNotNull(userBody);

		ResponseCookie accessCookie = user.getResponseCookies().get("accessToken").getFirst();
		ResponseCookie refreshCookie = user.getResponseCookies().get("refreshToken").getFirst();

		sout.print("[SOUT]: " + accessCookie.getName() + "=" + accessCookie.getValue());
		sout.print("[SOUT]: " + refreshCookie.getName() + "=" + refreshCookie.getValue());

		restTestClient.post().uri("/auth/refresh")
				.cookie(refreshCookie.getName(), "wrongToken")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
		EntityExchangeResult<?> refresh = restTestClient.post().uri("/auth/refresh")
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.returnResult();
		accessCookie = refresh.getResponseCookies().getFirst("accessToken");
		sout.print("[SOUT]: " + accessCookie.getName() + "=" + accessCookie.getValue());

		// user
        restTestClient.get().uri("/user/me")
				.accept(MediaType.APPLICATION_JSON)
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.json(String.format("{\"username\": \"%s\", \"createdAt\": \"%s\", \"fileCount\": %d}",
						userBody.getUsername(), userBody.getCreatedAt(), userBody.getFileCount()));

		EntityExchangeResult<Iterable> userAll = restTestClient.get().uri("/user/all")
				.accept(MediaType.APPLICATION_JSON)
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isOk()
				.expectBody(Iterable.class)
				.returnResult();
		userAll.getResponseBody().forEach(e -> sout.print("[SOUT]: [user/all]: " + e));

		restTestClient.get().uri("/user/changeFileCount")
				.accept(MediaType.APPLICATION_JSON)
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.FORBIDDEN);

		// file
		MultiValueMap<String, Resource> fileUploadBody = new LinkedMultiValueMap<>();
		FileSystemResource file = new FileSystemResource(new File("src/test/resources/test.txt"));
		fileUploadBody.add("file", file);
		EntityExchangeResult<String> uploadFile0 = restTestClient.post().uri("/file/upload")
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.body(fileUploadBody)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult();
		sout.print("[SOUT]: [file/upload]: " +  uploadFile0.getResponseBody());

		fileUploadBody.clear();

		fileUploadBody = new LinkedMultiValueMap<>();
		file = new FileSystemResource(new File("src/test/resources/test.png"));
		fileUploadBody.add("file", file);
		EntityExchangeResult<String> uploadFile1 = restTestClient.post().uri("/file/upload")
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.body(fileUploadBody)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult();
		sout.print("[SOUT]: [file/upload]: " +  uploadFile1.getResponseBody());

		restTestClient.get().uri("file/get?filename=test.png")
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isOk()
				.expectBody(Resource.class);

		EntityExchangeResult<String[]> fileAll = restTestClient.get().uri("/file/all")
				.accept(MediaType.APPLICATION_JSON)
				.cookie(accessCookie.getName(), accessCookie.getValue())
				.cookie(refreshCookie.getName(), refreshCookie.getValue())
				.exchange()
				.expectStatus().isOk()
				.expectBody(String[].class)
				.returnResult();
		for (String s : fileAll.getResponseBody()) {
			sout.print("[SOUT]: [file/all]: " + s);
		}
	}
}
