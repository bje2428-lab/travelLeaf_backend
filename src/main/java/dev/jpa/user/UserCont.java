package dev.jpa.user;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import dev.jpa.loginhistory.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession; // ✅ 추가

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserCont {

    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginHistoryService loginHistoryService;

    public UserCont() {
        System.out.println("-> UserCont created.");
    }

    @PostMapping("/save")
    public ResponseEntity<User> save(@RequestBody UserDTO userDTO) {
        User saved = userService.save(userDTO);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/check_id")
    public ResponseEntity<Integer> checkId(@RequestParam("userid") String userid) {
        return ResponseEntity.ok(userService.checkId(userid));
    }

    @GetMapping("/find_all")
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/read/{userno}")
    public ResponseEntity<User> read(@PathVariable("userno") Long userno) {
        return ResponseEntity.ok(userService.findByUserno(userno));
    }

    @PostMapping("/update_password")
    public ResponseEntity<Integer> updatePassword(@RequestBody UserDTO dto) {
        int sw;
        if (userService.countByUseridAndPassword(dto.getUserid(), dto.getPassword()) != 1) {
            sw = 2;
        } else {
            sw = userService.updatePassword(dto.getUserid(), dto.getNewPassword());
        }
        return ResponseEntity.ok(sw);
    }

    @DeleteMapping("/delete/{userno}")
    public ResponseEntity<?> delete(@PathVariable("userno") Long userno) {
        userService.delete(userno);
        return ResponseEntity.ok().build();
    }

    /* =========================
    🔥 로그인 (탈퇴 회원 차단 + 세션 저장)
 ========================= */
 @PostMapping("/login")
 public ResponseEntity<Map<String, Object>> login(
         @RequestBody UserDTO dto,
         HttpSession session,
         HttpServletRequest request
 ) {
   
   
     Map<String, Object> map = new HashMap<>();

     // 1️⃣ 아이디로 사용자 조회
     User u = userService.findByUserid(dto.getUserid());
     
     

     // ❌ 아이디 없음
     if (u == null) {
         map.put("cnt", 0);
         map.put("message", "아이디 또는 비밀번호가 틀렸습니다.");
         return ResponseEntity.ok(map);
     }

     // ❌ 탈퇴한 회원 차단
     if ("DELETE".equals(u.getStatus())) {
         map.put("cnt", 0);
         map.put("message", "탈퇴한 회원입니다.");
         return ResponseEntity.ok(map);
     }

     // ❌ 비밀번호 불일치
     if (!u.getPassword().equals(dto.getPassword())) {
         map.put("cnt", 0);
         map.put("message", "아이디 또는 비밀번호가 틀렸습니다.");
         return ResponseEntity.ok(map);
     }

     // ✅ 로그인 성공
     session.setAttribute("userId", u.getUserid());
     session.setAttribute("grade", u.getGrade());
     session.setAttribute("userno", u.getUserno());
     
     String ip = request.getRemoteAddr();
     String agent = request.getHeader("User-Agent");
     
     

     loginHistoryService.saveLoginHistory(
         u.getUserno(),
         ip,
         agent
     );

     map.put("cnt", 1);
     map.put("userno", u.getUserno());
     map.put("name", u.getName());
     map.put("userid", u.getUserid());
     map.put("email", u.getEmail());
     map.put("nickname", u.getNickname());
     map.put("phone", u.getPhone());
     map.put("gender", u.getGender());
     map.put("birth", u.getBirth());
     map.put("createdat", u.getCreatedat());
     map.put("grade", u.getGrade());

     return ResponseEntity.ok(map);
 }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody UserDTO dto) {
        Map<String, Object> map = new HashMap<>();
        try {
            User updated = userService.updateUser(dto.getUserid(), dto);
            map.put("success", true);
            map.put("user", updated);
        } catch (Exception e) {
            map.put("success", false);
            map.put("message", e.getMessage());
        }
        return ResponseEntity.ok(map);
    }

    // 🔥 닉네임 중복체크 (자기 자신 제외)
    @GetMapping("/check_nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(
            @RequestParam("nickname") String nickname,
            @RequestParam("userno") Long userno
    ) {
        boolean available = userService.isNicknameAvailable(nickname, userno);
        Map<String, Object> map = new HashMap<>();
        map.put("available", available);
        return ResponseEntity.ok(map);
    }
    
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody Map<String, String> map) {

        User user = userService.findUseridByNameAndEmail(
                map.get("name"),
                map.get("email")
        );

        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "일치하는 회원이 없습니다."));
        }

        return ResponseEntity.ok(
                Map.of("userid", user.getUserid())
        );
        
        
    }
    @PostMapping("/find-password")
    public ResponseEntity<?> findPassword(@RequestBody Map<String, String> map) {

        boolean result = userService.resetPassword(
                map.get("userid"),
                map.get("name"),
                map.get("email"),
                map.get("phone")
        );

        if (!result) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "입력한 정보와 일치하는 회원이 없습니다."));
        }

        return ResponseEntity.ok(
                Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다.")
        );
    }

    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        Page<User> result = userService.searchUsers(keyword, page, size);

        Map<String, Object> map = new HashMap<>();
        map.put("users", result.getContent());     // 현재 페이지 데이터
        map.put("currentPage", result.getNumber());
        map.put("totalItems", result.getTotalElements());
        map.put("totalPages", result.getTotalPages());

        return ResponseEntity.ok(map);
    }
    @PutMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        String userid = body.get("userid");

        userService.withdraw(userid);

        // 🔥 탈퇴 즉시 로그아웃
        session.invalidate();

        return ResponseEntity.ok(Map.of("message", "회원 탈퇴 완료"));
    }

 // 🔹 1. 프로필 이미지 업로드 (사용자가 직접 업로드)
    @PostMapping("/profile-image/upload")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("userno") Long userno,
            @RequestParam("file") MultipartFile file
    ) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 저장할 폴더
            String folder = "C:/kd/deploy/team3/images/";
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs(); // 폴더 없으면 생성

            // 1️⃣ 기존 이미지 삭제 (기본 이미지 제외)
            User user = userService.findByUserno(userno); // findById → findByUserno
            String currentImage = user.getProfileimage(); // getProfileImage → getProfileimage
            if (currentImage != null && !currentImage.equals("/images/기본이미지.jpg")) {
                File oldFile = new File("C:/kd/deploy/team3" + currentImage);
                if (oldFile.exists()) oldFile.delete();
            }

            // 2️⃣ 새 파일명 UUID로 생성
            String originalExtension = file.getOriginalFilename().substring(
                    file.getOriginalFilename().lastIndexOf(".")
            );
            String filename = UUID.randomUUID().toString() + originalExtension;
            String filePath = folder + filename;

            // 3️⃣ 파일 저장
            file.transferTo(new File(filePath));

            // 4️⃣ DB에 저장할 URL
            String imageUrl = "/images/" + filename;
            User updated = userService.updateProfileImage(userno, imageUrl);

            map.put("success", true);
            map.put("user", updated);
            map.put("imageUrl", imageUrl);
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그 확인용
            map.put("success", false);
            map.put("message", e.getMessage());
        }
        return ResponseEntity.ok(map);
    }




 // 🔹 AI 이미지 생성 전용 (파일 저장, 프로필 등록 X)
 // 🔹 AI 이미지 생성 전용 (파일 저장, 프로필 등록 X)
    @PostMapping("/ai-image")
    public ResponseEntity<Map<String, Object>> generateAIImageOnly(
            @RequestParam("prompt") String prompt
    ) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 🔹 프롬프트 체크
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new IllegalArgumentException("프롬프트가 비어있습니다.");
            }

            // 🔹 AI 이미지 생성
            String imageUrl = userService.generateAIImageWithTranslate(prompt);


            map.put("success", true);
            map.put("imageUrl", imageUrl);  // 사용자에게 URL만 전달
        } catch (Exception e) {
            e.printStackTrace(); // 🔹 서버 콘솔에서 상세 에러 확인
            map.put("success", false);
            map.put("message", e.getMessage() != null ? e.getMessage() : "서버 오류(콘솔 로그 확인)");
        }
        return ResponseEntity.ok(map);
    }



    @PostMapping("/profile-image/delete") // POST로 통일
    public ResponseEntity<Map<String, Object>> deleteProfileImage(@RequestParam("userno") Long userno) {
        Map<String, Object> map = new HashMap<>();
        try {
            User updated = userService.deleteProfileImage(userno); // 서비스 호출
            map.put("success", true);
            map.put("imageUrl", updated.getProfileimage());
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("message", e.getMessage());
        }
        return ResponseEntity.ok(map);
    }
    @PostMapping("/profile-image/save-ai")
    public ResponseEntity<Map<String,String>> saveAiProfileImage(
            @RequestBody Map<String, String> data) throws IOException {

        String imageUrl = data.get("imageUrl");
        Long userno = Long.valueOf(data.get("userno"));

        // 1️⃣ 외부 URL에서 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage img = ImageIO.read(url);

        // 2️⃣ 서버 로컬에 저장
        String folder = "C:/kd/deploy/team3/images/";
        File dir = new File(folder);
        if (!dir.exists()) dir.mkdirs();

        String filename = UUID.randomUUID().toString() + ".png";
        File outFile = new File(folder + filename);
        ImageIO.write(img, "png", outFile);

        // 3️⃣ DB 업데이트
        String dbPath = "/images/" + filename;
        userService.updateProfileImage(userno, dbPath);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", dbPath);
        return ResponseEntity.ok(response);
    }
   }



