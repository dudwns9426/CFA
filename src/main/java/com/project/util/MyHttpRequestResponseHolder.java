//package com.project.util;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.springframework.security.web.context.HttpRequestResponseHolder;
//
//
//public class MyHttpRequestResponseHolder {
//
//    private HttpRequestResponseHolder holder;
//
//    public MyHttpRequestResponseHolder() {
//        this.holder = new HttpRequestResponseHolder(null, null);
//    }
//
//    public HttpRequestResponseHolder getHolder() {
//        return holder;
//    }
//
//    public void setRequestAndResponse(HttpServletRequest request, HttpServletResponse response) {
//        holder = new HttpRequestResponseHolder(request, response);
//    }
//}