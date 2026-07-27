package dev.jpa.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;



@Entity
@Table(name = "USER_TB")
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_no_seq")
    @SequenceGenerator(
        name = "user_no_seq",
        sequenceName = "USER_NO_SEQ",
        allocationSize = 1
    )
    @Column(name = "USER_NO")
    private Long userno;

    @Column(name = "USER_ID", unique = true, nullable = false, length = 50)
    private String userid;
    
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "EMAIL", unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 50)
    private String password;

    @Column(name = "NICKNAME", unique = true, nullable = false, length = 50)
    private String nickname;

    @Column(name = "PROFILE_IMAGE", length = 700)
    private String profileimage = "/images/기본이미지.jpg";


    @Column(name = "PHONE", unique = true, length = 20)
    private String phone;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "CREATED_AT")
    private LocalDate createdat;



    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "GRADE", nullable = false)
    private Integer grade;

    @Column(name = "BIRTH")
    private LocalDate birth;

    public User() {}

    public User(String userid,String name, String email, String password, String nickname,
                String profileimage, String phone, String gender,
                LocalDate createdat, String status, Integer grade, LocalDate birth) {
        this.userid = userid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileimage = profileimage;
        this.phone = phone;
        this.gender = gender;
        this.createdat = createdat;
        this.status = status;
        this.grade = grade;
        this.birth = birth;
    }

    public User(Long userno,String name, String userid, String email, String password, String nickname,
                String profileimage, String phone, String gender,
                LocalDate createdat, String status, Integer grade, LocalDate birth) {
        this.userno = userno;
        this.name = name;
        this.userid = userid;
               this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileimage = profileimage;
        this.phone = phone;
        this.gender = gender;
        this.createdat = createdat;
        this.status = status;
        this.grade = grade;
        this.birth = birth;
    }
}
