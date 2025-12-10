package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.UserMapper;   // hoặc UserAccountMapper, nhớ chỉnh đúng tên
import com.jpvocab.vocabsite.model.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")   // có WebConfig global thì dòng này có thể bỏ
public class AuthController {

    @Autowired
    private UserMapper userMapper;  // Đảm bảo interface này có findByUsername + insertUser

    // ----- DTO -----
    public static class RegisterRequest {
        public String username;
        public String password;
        public String fullName;
        public String email;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class AuthResponse {
        public boolean success;
        public String message;
        public UserAccount user;

        public AuthResponse(boolean success, String message, UserAccount user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }

    // ----- ĐĂNG KÝ -----
    // ----- ĐĂNG KÝ -----
    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        // 1) Check username trùng trước
        UserAccount existing = userMapper.findByUsername(req.username);
        if (existing != null) {
            return new AuthResponse(false, "Tên đăng nhập đã tồn tại", null);
        }

        // 2) Hash password
        String hash = BCrypt.hashpw(req.password, BCrypt.gensalt());

        // 3) Tạo entity
        UserAccount user = new UserAccount();
        user.setUsername(req.username);
        user.setPasswordHash(hash);
        user.setFullName(req.fullName);
        user.setEmail(req.email);
        user.setRole("USER");

        try {
            // 4) Insert DB – có thể ném DuplicateKeyException nếu email trùng
            userMapper.insertUser(user);
        } catch (DuplicateKeyException ex) {
            // Mặc định
            String msg = "Tài khoản hoặc email đã tồn tại";

            // Nếu message chứa tên unique index email thì báo chính xác hơn
            String causeMsg = ex.getMostSpecificCause() != null
                    ? ex.getMostSpecificCause().getMessage()
                    : ex.getMessage();

            if (causeMsg != null && causeMsg.contains("uq_user_email")) {
                msg = "Email đã tồn tại";
            }
            // TODO: log ra nếu muốn: ex.printStackTrace() hoặc logger.error(...);

            return new AuthResponse(false, msg, null);
        }

        // 5) Không trả password hash về client
        user.setPasswordHash(null);

        return new AuthResponse(true, "Đăng ký thành công", user);
    }


    // ----- ĐĂNG NHẬP -----
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        UserAccount db = userMapper.findByUsername(req.username);
        if (db == null) {
            return new AuthResponse(false, "Sai tài khoản hoặc mật khẩu.", null);
        }

        if (!BCrypt.checkpw(req.password, db.getPasswordHash())) {
            return new AuthResponse(false, "Sai tài khoản hoặc mật khẩu.", null);
        }

        db.setPasswordHash(null);
        return new AuthResponse(true, "Đăng nhập thành công", db);
    }
}
