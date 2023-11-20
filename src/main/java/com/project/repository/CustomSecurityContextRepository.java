//package com.project.repository;
//
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.context.HttpRequestResponseHolder;
//import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
//import org.springframework.security.web.context.SecurityContextRepository;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//@Component
//public class CustomSecurityContextRepository implements SecurityContextRepository {
//	@Override
//    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
//        HttpServletRequest request = requestResponseHolder.getRequest();
//        SecurityContext context = (SecurityContext) request.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
//
//        if (context == null) {
//            context = SecurityContextHolder.createEmptyContext();
//        }
//
//        return context;
//    }
//
//    @Override
//    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
//        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", context);
//    }
//
//    @Override
//    public boolean containsContext(HttpServletRequest request) {
//        return request.getSession().getAttribute("SPRING_SECURITY_CONTEXT") != null;
//    }
////    public SecurityContext loadContext(HttpServletRequest request) {
////        // 세션에서 SecurityContext를 로드하거나 초기화된 SecurityContext를 반환합니다.
////        SecurityContext context = null;
////        HttpSession session = request.getSession(false); // 세션이 존재하면 가져오고, 존재하지 않으면 null 반환
////
////        if (session != null) {
////            context = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
////        }
////        if (context == null) {
////            context = SecurityContextHolder.createEmptyContext();
////        }
////        return context;
////    }
////
////    @Override
////    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
////        // SecurityContext를 세션에 저장합니다.
////        HttpSession session = request.getSession(true); // 세션을 가져오거나 새로운 세션을 생성
////        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
////    }
////
////    @Override
////    public boolean containsContext(HttpServletRequest request) {
////        // 세션에 SecurityContext가 존재하는지 확인합니다.
////        HttpSession session = request.getSession(false); // 세션이 존재하면 가져오고, 존재하지 않으면 null 반환
////
////        if (session != null) {
////            return session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) != null;
////        }
////        return false;
////    }
////
////	@Override
////	public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
////		// TODO Auto-generated method stub
////		return null;
////	}
//}
