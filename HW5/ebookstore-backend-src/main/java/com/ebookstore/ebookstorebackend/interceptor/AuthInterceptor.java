package com.ebookstore.ebookstorebackend.interceptor;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 拦截器 用来拦截认证的请求
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.ebookstore.ebookstorebackend.service.UserService;
import java.io.IOException;
// @NonNull注解用于标记某个参数、方法返回值或字段不能为null
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;




    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception{
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("=== AuthInterceptor Begin ===");
        System.out.println("request: " + method + " " + requestURI);

        // 检查是否为公开路径
        if (isPublicPath(requestURI, method)) {
            System.out.println("公开路径，允许访问");
            return true;
        }

        // 检查HttpSession
        HttpSession session = request.getSession(false); // false表示如果没有session不创建新的

        if (session == null) {
            System.out.println("无Session，拒绝访问");
            sendUnauthorizedResponse(response);
            return false;
        }
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        String userAccount = (String) session.getAttribute("userAccount");
        Object userObj = session.getAttribute("user");

        System.out.println("Session ID: " + session.getId());
        System.out.println("登录状态: " + isLoggedIn);
        System.out.println("用户账号: " + userAccount);
        System.out.println("用户对象: " + (userObj != null ? "存在" : "不存在"));        // 检查session中是否有user对象或者userAccount
        boolean hasValidSession = (isLoggedIn != null && isLoggedIn) && 
                                 (userAccount != null || userObj != null);
        
        if (!hasValidSession) {
            System.out.println("用户未登录，拒绝访问");
            sendUnauthorizedResponse(response);
            return false;
        }

        // 检查用户是否被禁用
        if (userAccount != null && !userService.isUserActive(userAccount)) {
            System.out.println("用户已被禁用，拒绝访问");
            sendDisabledUserResponse(response);
            return false;
        }

        System.out.println("用户已登录且状态正常，允许访问");
        return true;
    }
    private boolean isPublicPath(String requestURI, String method) {
        // OPTIONS 请求永远放行（CORS预检请求）
        if ("OPTIONS".equals(method)) {
            System.out.println("CORS预检请求，放行");
            return true;
        }
        
        // 登录相关接口
        if (requestURI.equals("/api/users/login") ||
                requestURI.equals("/api/users/check-session") ||
                requestURI.startsWith("/api/users/check-username/") ||
                requestURI.equals("/api/users/register"))

                {
            return true;
        }

        // // 图书浏览接口（GET请求）- 允许未登录用户浏览
        // if (requestURI.startsWith("/api/books") && "GET".equals(method)) {
        //     return true;
        // }

        // // 静态资源
        // if (requestURI.startsWith("/static/") ||
        //         requestURI.startsWith("/css/") ||
        //         requestURI.startsWith("/js/") ||
        //         requestURI.startsWith("/images/")) {
        //     return true;
        // }

        return false;
    }    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        // response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        // response.setHeader("Access-Control-Allow-Credentials", "true");

        String errorJson = """
            {
                "error": "未登录或session已过期",
                "code": 401,
                "message": "请先登录"
            }
            """;
        response.getWriter().write(errorJson);
    }

    /**
     * 发送用户被禁用响应
     */
    private void sendDisabledUserResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        // response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        // response.setHeader("Access-Control-Allow-Credentials", "true");

        String errorJson = """
            {
                "error": "用户账户已被禁用",
                "code": 403,
                "message": "您的账户已被禁用，如有疑问请联系管理员"
            }
            """;
        response.getWriter().write(errorJson);
    }

}


