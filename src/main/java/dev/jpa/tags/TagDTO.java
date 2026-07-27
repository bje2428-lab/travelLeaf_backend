package dev.jpa.tags;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDTO {
private Long tagId;


@NotBlank(message = "태그 이름은 필수입니다.")
@Size(max = 100, message = "태그 이름은 최대 100자입니다.")
private String name;
}