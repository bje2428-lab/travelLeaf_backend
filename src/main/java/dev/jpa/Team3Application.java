package dev.jpa;

import dev.jpa.tool.Tool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication   // ⬅ 이거 하나면 dev.jpa 아래 전부 자동 스캔됨
@EnableScheduling
public class Team3Application {

    public static void main(String[] args) {
        SpringApplication.run(Team3Application.class, args);
    }

    // ✨ 파일 업로드된 리소스 경로를 웹으로 노출하는 설정 ✨
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {

        return new WebMvcConfigurer() {

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {

                // notice 모듈 파일 경로
                String noticeStoragePath = Tool.getServerDir("notice");

                // contents 모듈 파일 경로
                String contentsStoragePath = Tool.getServerDir("contents");

                // posts 모듈 파일 경로
                String postsStoragePath = Tool.getServerDir("posts");

                // notice 리소스 매핑
                registry.addResourceHandler("/notice/storage/**")
                        .addResourceLocations("file:" + noticeStoragePath)
                        .setCachePeriod(3600);

                // contents 리소스 매핑
                registry.addResourceHandler("/contents/storage/**")
                        .addResourceLocations("file:" + contentsStoragePath)
                        .setCachePeriod(3600);

                // posts 리소스 매핑
                registry.addResourceHandler("/posts/storage/**")
                        .addResourceLocations("file:" + postsStoragePath)
                        .setCachePeriod(3600);

                // 기본 정적 리소스
                registry.addResourceHandler("/**")
                        .addResourceLocations(
                                "classpath:/static/",
                                "classpath:/public/"
                        )
                        .setCachePeriod(3600);
            }
        };
    }
}
