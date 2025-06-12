package com.test.Controller;

import com.test.Mapper.UsersMapper;
import com.test.Pojo.Users;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class LoginController {

    @Resource
    private UsersMapper usersMapper;
    
    private static final String SECRET_KEY = "yourSecretKey";

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String ToIndex() {
        return "index";
    }
    
    @GetMapping("/api/user-info")
    @ResponseBody
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
                
                result.put("username", claims.getSubject());
                result.put("role", claims.get("role"));
                result.put("id", claims.get("id"));
                result.put("success", true);
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "无效token");
            }
        } else {
            result.put("success", false);
            result.put("message", "未提供token");
        }
        
        return result;
    }

    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> doLogin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") Boolean rememberMe,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            Users user = usersMapper.findByEmail(email);

            // 验证用户存在且密码正确
            if (user != null && password.equals(user.getPassword())) {
                // 登录成功，将用户信息存入session
                session.setAttribute("user", user);

                // 如果选择"记住我"，延长session过期时间
                if (rememberMe) {
                    session.setMaxInactiveInterval(7 * 24 * 60 * 60);
                }

                // 生成JWT Token
                String token = Jwts.builder()
                        .setSubject(user.getUsername())
                        .claim("role", user.getRole())
                        .claim("id", user.getId())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1天
                        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                        .compact();

                result.put("success", true);
                result.put("message", "登录成功");
                result.put("token", token); // 返回token
            } else {
                result.put("success", false);
                result.put("message", "邮箱或密码错误");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "登录失败");
        }

        return result;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 清除session
        session.invalidate();
        // 重定向到登录页
        return "redirect:/login";
    }
    
    @PostMapping("/api/logout")
    @ResponseBody
    public Map<String, Object> apiLogout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        // 清除session
        session.invalidate();
        result.put("success", true);
        result.put("message", "退出成功");
        return result;
    }
}