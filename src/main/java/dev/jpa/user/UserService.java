package dev.jpa.user;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class UserService {

  @Value("${openai.api-key}") // application.yml 키 불러오기
  private String apiKey;

  
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    /* =========================
       회원가입
    ========================= */
    public User save(UserDTO dto) {
        User user = dto.toEntity();
        user.setGrade(1); // 기본 등급
        return userRepository.save(user);
    }

    /* =========================
       아이디 중복 체크
    ========================= */
    public int checkId(String userid) {
        return userRepository.countByUserid(userid);
    }

    /* =========================
       전체 조회
    ========================= */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /* =========================
       회원 상세 조회
    ========================= */
    public User findByUserno(Long userno) {
        return userRepository.findById(userno).orElse(null);
    }

    /* =========================
       로그인 체크
    ========================= */
    public int countByUseridAndPassword(String userid, String password) {
        return userRepository.countByUseridAndPassword(userid, password);
    }

    /* =========================
       비밀번호 변경
    ========================= */
    public int updatePassword(String userid, String newpassword) {
        return userRepository.updatePassword(userid, newpassword);
    }

    /* =========================
       아이디로 유저 조회
    ========================= */
    public User findByUserid(String userid) {
        return userRepository.findByUserid(userid).orElse(null);
    }
    
   

    /* =========================
       회원 삭제
    ========================= */
    public void delete(Long userno) {
        userRepository.deleteById(userno);
    }

    /* =========================
       닉네임 중복 체크
    ========================= */
    public boolean isNicknameAvailable(String nickname, Long userno) {
        Optional<User> opt = userRepository.findByNickname(nickname);

        if (opt.isEmpty()) return true;

        User found = opt.get();
        return found.getUserno().equals(userno);
    }

    /* =========================
       회원 정보 수정
    ========================= */
    public User updateUser(String userid, UserDTO dto) {

        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirth() != null) user.setBirth(dto.getBirth());

        return userRepository.save(user);
    }

    /* =========================
       유저 등급 조회
    ========================= */
    public int getUserGrade(String userId) {
        return userRepository.findByUserid(userId)
                .map(User::getGrade)
                .orElse(0);
    }

    /* =========================
       아이디 찾기
    ========================= */
    public User findUseridByNameAndEmail(String name, String email) {
        return userRepository.findByNameAndEmail(name, email)
                .orElse(null);
    }

    /* =========================
       🔥 비밀번호 찾기 (임시 비밀번호 발급)
    ============================= =======*/
    public boolean resetPassword(
            String userid,
            String name,
            String email,
            String phone
    ) {

        Optional<User> opt =
                userRepository.findByUseridAndNameAndEmailAndPhone(
                        userid, name, email, phone
                );

        if (opt.isEmpty()) {
            return false;
        }

        User user = opt.get();

        // 1️⃣ 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

        // 2️⃣ 비밀번호 업데이트
        user.setPassword(tempPassword);
        userRepository.save(user);

        // 3️⃣ 이메일 전송
        sendTempPasswordMail(user.getEmail(), tempPassword);

        return true;
    }

    /* =========================
       임시 비밀번호 생성
    ========================= */
    private String generateTempPassword() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10);
    }

    /* =========================
       이메일 전송
    ========================= */
    private void sendTempPasswordMail(String toEmail, String tempPassword) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[비밀번호 재설정 안내]");
        message.setText(
                "임시 비밀번호가 발급되었습니다.\n\n"
              + "임시 비밀번호: " + tempPassword + "\n\n"
              + "로그인 후 반드시 비밀번호를 변경해주세요."
        );

        mailSender.send(message);
    }
    
    public User login(String userid, String password) {
      return userRepository.findByUserid(userid)
              .filter(u -> u.getPassword().equals(password))
              .orElseThrow(() ->
                  new RuntimeException("아이디 또는 비밀번호가 틀렸습니다.")
              );
  }
    
    public Page<User> searchUsers(String keyword, int page, int size) {

      Pageable pageable = PageRequest.of(page, size);

      // keyword 하나로 여러 컬럼 검색
      return userRepository
              .findByUseridContainingOrNameContainingOrEmailContainingOrNicknameContaining(
                      keyword,
                      keyword,
                      keyword,
                      keyword,
                      pageable
              );

}
    public void withdraw(String userid) {

      User user = userRepository.findByUserid(userid)
              .orElseThrow(() -> new RuntimeException("사용자 없음"));

      if ("DELETE".equals(user.getStatus())) {
          throw new RuntimeException("이미 탈퇴한 회원");
      }

      String suffix = String.valueOf(System.currentTimeMillis());

      userRepository.withdraw(userid);
  }
    public void sendSuspiciousLoginMail(
        String toEmail,
        String reason
) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("[보안 알림] 의심스러운 로그인 감지");
    message.setText(
        "안녕하세요.\n\n"
      + "회원님의 계정에서 평소와 다른 로그인 시도가 감지되었습니다.\n\n"
      + "AI 분석 결과:\n"
      + reason + "\n\n"
      + "본인이 아니라면 비밀번호를 즉시 변경해 주세요."
    );

    mailSender.send(message);
}
    public User updateProfileImage(Long userno, String imageUrl) {
      User user = userRepository.findById(userno)
              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
      user.setProfileimage(imageUrl);
      return userRepository.save(user);
  }
    public String generateAIImage(String userPrompt) {
      try {
          // 1️⃣ 사용자 입력 검증
          if (userPrompt == null || userPrompt.trim().isEmpty()) {
              throw new IllegalArgumentException("프롬프트가 비어있습니다.");
          }

          // 2️⃣ 프롬프트 구조: 객체 중심 + 지브리풍 + 디테일 강조
          String finalPrompt = "Focus on the main subject: " + userPrompt
                             + ", close-up, in the style of Studio Ghibli, highly detailed, soft colors,cute ,whimsical, magical, cinematic lighting, professional illustration";

          // 3️⃣ OpenAI 이미지 생성 API 호출
          String url = "https://api.openai.com/v1/images/generations";

          RestTemplate restTemplate = new RestTemplate();

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          headers.set("Authorization", "Bearer " + apiKey);

          JSONObject body = new JSONObject();
          body.put("prompt", finalPrompt);
          body.put("size", "512x512"); // 해상도 강화
          body.put("n", 1);

          HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

          ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

          // 4️⃣ 결과 파싱
          JSONObject responseJson = new JSONObject(response.getBody());
          String imageUrl = responseJson.getJSONArray("data")
                  .getJSONObject(0)
                  .getString("url");

          return imageUrl;

      } catch (Exception e) {
          throw new RuntimeException("AI 이미지 생성 실패: " + e.getMessage(), e);
      }
  }

  
    public User deleteProfileImage(Long userno) {
      User user = userRepository.findById(userno)
              .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

      String currentImage = user.getProfileimage();

      // 기본 이미지가 아니면 파일 삭제
      if (currentImage != null && !currentImage.equals("/images/기본이미지.jpg")) {
          File oldFile = new File("C:/kd/deploy/team3" + currentImage);
          if (oldFile.exists()) oldFile.delete();
      }

      // DB 값 기본 이미지로 변경
      user.setProfileimage("/images/기본이미지.jpg");
      return userRepository.save(user); // ✅ save(User) 호출 -> 문제 해결
  }
    public String translateKoToEn(String koreanText) {
      try {
          String url = "https://api.openai.com/v1/chat/completions";

          RestTemplate restTemplate = new RestTemplate();

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          headers.set("Authorization", "Bearer " + apiKey);

          JSONObject body = new JSONObject();
          body.put("model", "gpt-4o-mini");

          JSONArray messages = new JSONArray();
          messages.put(new JSONObject()
                  .put("role", "system")
                  .put("content", "You are a professional translator."));
          messages.put(new JSONObject()
                  .put("role", "user")
                  .put("content",
                          "Translate the following Korean text into natural English for an AI image prompt:\n"
                          + koreanText));

          body.put("messages", messages);

          HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
          ResponseEntity<String> response =
                  restTemplate.postForEntity(url, request, String.class);

          JSONObject resJson = new JSONObject(response.getBody());

          return resJson
                  .getJSONArray("choices")
                  .getJSONObject(0)
                  .getJSONObject("message")
                  .getString("content")
                  .trim();

      } catch (Exception e) {
          // 번역 실패 시 원문 그대로 사용 (안전장치)
          return koreanText;
      }
  }
    public String generateAIImageWithTranslate(String userPrompt) {
      // 1️⃣ 한글 → 영어 번역
      String translatedPrompt = translateKoToEn(userPrompt);

      // 2️⃣ 이미지 생성 (기존 로직 재사용)
      return generateAIImage(translatedPrompt);
  }

}
