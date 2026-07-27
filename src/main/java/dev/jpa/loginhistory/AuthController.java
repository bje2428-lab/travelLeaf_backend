package dev.jpa.loginhistory;

import dev.jpa.user.User;
import dev.jpa.user.UserService;
import dev.jpa.loginhistory.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession; 
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final LoginHistoryService loginHistoryService;

    public AuthController(UserService userService,
                          LoginHistoryService loginHistoryService) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest req,
                      HttpServletRequest request,
                      HttpSession session) {

        User user = userService.login(req.getUserid(), req.getPassword());

        // ⭐ 로그인 성공 시 기록
        loginHistoryService.saveLoginHistory(
            user.getUserno(),
            request.getRemoteAddr(),
            request.getHeader("User-Agent")
        );
        
        session.setAttribute("userno", user.getUserno());
        session.setAttribute("userId", user.getUserid());

        return user;
    }
    
}
