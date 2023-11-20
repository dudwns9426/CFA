//package com.project.util;
//
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class CustomSecurityContextManager {
//    private ThreadLocal<SecurityContext> contextHolder = ThreadLocal.withInitial(SecurityContextHolder::createEmptyContext);
//
//    public SecurityContext getContext() {
//        return contextHolder.get();
//    }
//
//    public void setContext(SecurityContext context) {
//        contextHolder.set(context);
//    }
//
//    public void clearContext() {
//        contextHolder.remove();
//    }
//}
