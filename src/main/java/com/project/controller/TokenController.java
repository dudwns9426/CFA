//package com.project.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//import com.project.domain.dto.RefreshTokenRequest;
//import com.project.service.TokenService;
//
//
//@Controller
//public class TokenController {
//
//    @Autowired
//    private TokenService tokenService;
//
//	@PostMapping("/tokenrefresh")
//    public ResponseEntity<String> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
//		
//        return tokenService.refreshAccessToken(request.getRefreshToken());
//    }
//}
