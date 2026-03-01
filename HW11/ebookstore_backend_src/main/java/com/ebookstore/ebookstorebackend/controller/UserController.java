package com.ebookstore.ebookstorebackend.controller;


import com.ebookstore.ebookstorebackend.dto.UserDTO;
import com.ebookstore.ebookstorebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;



@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class UserController {
  private final UserService userService;
  // final 关键字意味着，一旦 userService 这个变量被初始化并指向一个 UserService 对象之后，就不能再让它指向别的 UserService 对象了
  
  // Spring 会自动查找并注入 UserService 的一个实例
  // @Autowired 的核心作用就是让 Spring 容器帮你管理对象之间的依赖关系，你只需要声明你需要什么类型的对象，Spring 就会负责把对应的 Bean “装配”进来
  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }
  // @PostMapping 用于将 HTTP Post请求映射到特定的处理方法上。  
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
    String username = loginRequest.get("username");
    String password = loginRequest.get("password");
        
    System.out.println("用户 " + username + " 请求登录 ");
    
    // 先检查用户是否存在
    Optional<UserDTO> userProfile = userService.getUserProfile(username);
    if (userProfile.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "用户名或密码错误");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    // 检查用户是否被禁用
    if (!userService.isUserActive(username)) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "账户已被禁用，请联系管理员");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    // 检查密码
    boolean authenticated = userService.authenticate(username, password);
    if (authenticated) {
      // 创建会话
      HttpSession session = request.getSession();
      session.setAttribute("isLoggedIn", true);
      session.setAttribute("userAccount", username);
      
      // 获取用户对象并存储到会话中
      userService.getUserProfile(username).ifPresent(userDTO -> {
        // 直接存储User实体到session中
        session.setAttribute("user", userDTO.getUser());
      });
      
      session.setMaxInactiveInterval(15 * 60); // 15分钟过期
      
      System.out.println("用户 " + username + " 登录成功，Session ID: " + session.getId());
      
      Map<String, Object> response = new HashMap<>();
      userService.getUserProfile(username).ifPresent(user -> {
        response.put("user", user);
      });
      response.put("success", true);
      response.put("message", "登录成功");
      return ResponseEntity.ok(response);
    }
    else {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "用户名或密码错误");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }
  
  @GetMapping("/check-session")
  public ResponseEntity<?> checkSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    Map<String, Object> response = new HashMap<>();
    
    if (session != null && session.getAttribute("isLoggedIn") != null && 
        (Boolean) session.getAttribute("isLoggedIn")) {
      String userAccount = (String) session.getAttribute("userAccount");
      
      response.put("isLoggedIn", true);
      response.put("userAccount", userAccount);
      
      // 获取用户详细信息
      userService.getUserProfile(userAccount).ifPresent(user -> {
        response.put("user", user);
      });
      
      System.out.println("Session检查成功，用户: " + userAccount);
      return ResponseEntity.ok(response);
    } else {
      response.put("isLoggedIn", false);
      System.out.println("Session检查失败，用户未登录");
      return ResponseEntity.ok(response);
    }
  }
    // GET 方法的登录接口，用于使用URL参数进行登录测试
  @GetMapping("/login")
  public ResponseEntity<?> loginWithGet(
          @RequestParam String username,
          @RequestParam String password,
          HttpServletRequest request) {
    
    boolean authenticated = userService.authenticate(username, password);
    System.out.println("用户 " + username + " 通过GET请求登录 ");
    
    if (authenticated) {
      // 创建会话
      HttpSession session = request.getSession();
      session.setAttribute("isLoggedIn", true);
      session.setAttribute("userAccount", username);
      
      // 获取用户对象并存储到会话中
      userService.getUserProfile(username).ifPresent(userDTO -> {
        session.setAttribute("user", userDTO.getUser());
      });
      
      session.setMaxInactiveInterval(5 * 60); // 5分钟过期
      
      System.out.println("用户 " + username + " 登录成功，Session ID: " + session.getId());
      
      Map<String, Object> response = new HashMap<>();
      userService.getUserProfile(username).ifPresent(user -> {
        response.put("user", user);
      });
      response.put("success", true);
      response.put("message", "登录成功");
      return ResponseEntity.ok(response);
    } else {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "用户名或密码错误");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    Map<String, Object> response = new HashMap<>();
    
    if (session != null) {
      String userAccount = (String) session.getAttribute("userAccount");
      session.invalidate(); // 销毁会话
      System.out.println("用户 " + userAccount + " 已登出");
    }
    
    response.put("success", true);
    response.put("message", "登出成功");
    return ResponseEntity.ok(response);
  }
  
  @GetMapping("/profile/{account}")
  public ResponseEntity<?> getUserProfile(@PathVariable String account) {
    return userService.getUserProfile(account)
      .map(profile -> ResponseEntity.ok(profile))
      .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/profile/{account}")
  public ResponseEntity<?> updateUserProfile(
    @PathVariable String account,
    @RequestBody UserDTO user) {
    try {
      UserDTO updatedProfile = userService.updateUserProfile(account, user);
      return ResponseEntity.ok(updatedProfile);
    }
    catch (Exception e) {
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  // 在现有的 UserController 中添加注册方法

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String username = registerRequest.get("username");
            String password = registerRequest.get("password");
            String confirmPassword = registerRequest.get("confirmPassword");
            String email = registerRequest.get("email");
            
            System.out.println("=== 用户注册请求 ===");
            System.out.println("用户名: " + username);
            System.out.println("邮箱: " + email);
            
            // 1. 验证必填字段
            if (username == null || username.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (password == null || password.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "密码不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "邮箱不能为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 2. 验证用户名格式（3-20字符，只允许字母、数字、下划线）
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名格式不正确：3-20个字符，只能包含字母、数字和下划线");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 3. 验证密码长度（至少6个字符）
            if (password.length() < 6) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "密码长度至少6个字符");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 4. 验证两次密码是否一致
            if (!password.equals(confirmPassword)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "两次输入的密码不一致");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 5. 验证邮箱格式
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!email.matches(emailRegex)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "邮箱格式不正确");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // 6. 检查用户名是否已存在
            if (userService.getUserProfile(username).isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名已存在，请选择其他用户名");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // 7. 检查邮箱是否已被使用
            if (userService.isEmailExists(email)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该邮箱已被注册，请使用其他邮箱");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // 8. 创建新用户
            UserDTO createdUser = userService.createUser(username, password, email);
            
            System.out.println("用户注册成功: " + username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", createdUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("注册失败: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }    // 添加检查用户名是否存在的接口
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> checkUsername(@PathVariable String username) {
        try {
            boolean exists = userService.getUserProfile(username).isPresent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("available", !exists);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
