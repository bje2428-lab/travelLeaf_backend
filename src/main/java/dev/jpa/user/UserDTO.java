package dev.jpa.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonFormat; // ⭐ 추가!

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {

    private Long userno;
    private String name;
    private String userid;
    private String email;
    private String password;
    private String nickname;
    private String profileimage;
    private String phone;
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdat;

    private String status;
    private Integer grade;
    private String newPassword;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birth;

    public User toEntity() {
        return new User(
            this.userid,
            this.name,
            this.email,
            this.password,
            this.nickname,
            this.profileimage,
            this.phone,
            this.gender,
            this.createdat,
            this.status,
            this.grade,
            this.birth
        );
    }
    
    public class AiProfileImageDTO {
      private String imageUrl;
      private Long userno;

      public String getImageUrl() { return imageUrl; }
      public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

      public Long getUserno() { return userno; }
      public void setUserno(Long userno) { this.userno = userno; }
  }

}
