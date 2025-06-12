package com.test.Controller;

import com.test.Mapper.UsersMapper;
import com.test.Pojo.Users;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class RegisterController {

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @Resource
    JavaMailSender sender;
    
    @Resource
    UsersMapper usersMapper;

    @PostMapping("/code")//发送验证码
    @ResponseBody
    public Map<String, Object> getCode(@RequestParam String email, HttpSession session){
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证邮箱是否已经注册
            Users existingUser = usersMapper.findByEmail(email);
            if (existingUser != null) {
                result.put("success", false);
                result.put("message", "该邮箱已注册");
                return result;
            }
            
            Random random = new Random();
            int code = random.nextInt(900000) + 100000;
            session.setAttribute("code", code);
            // 保存邮箱到session，用于后续验证
            session.setAttribute("codeEmail", email);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("验证码");
            message.setText("您的验证码是: " + code);
            message.setFrom("zzzyt217@163.com");
            message.setTo(email);
            sender.send(message);
            
            result.put("success", true);
            result.put("message", "验证码已发送");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送失败");
        }
        
        return result;
    }
    
    @PostMapping("/register")//获得验证码后注册用户
    @ResponseBody
    public Map<String, Object> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String verificationCode,
            @RequestParam String role,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证码校验
            Integer savedCode = (Integer) session.getAttribute("code");
            String savedEmail = (String) session.getAttribute("codeEmail");
            
            // 验证邮箱一致性
            if (savedEmail == null || !savedEmail.equals(email)) {
                result.put("success", false);
                result.put("message", "注册邮箱与验证码邮箱不一致");
                return result;
            }
            
            // 验证验证码
            if (savedCode == null || !savedCode.toString().equals(verificationCode)) {
                result.put("success", false);
                result.put("message", "验证码错误");
                return result;
            }
            
            // 创建新用户
            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setEemail(email);
            newUser.setPassword(password);
            newUser.setRole(role);
            
            usersMapper.insert(newUser);
            result.put("success", true);
            result.put("message", "注册成功");
            
            // 清除session中的验证码和邮箱
            session.removeAttribute("code");
            session.removeAttribute("codeEmail");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "注册失败");
        }
        
        return result;
    }
}
