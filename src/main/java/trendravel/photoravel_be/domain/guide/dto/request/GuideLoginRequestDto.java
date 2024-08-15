package trendravel.photoravel_be.domain.guide.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.http.MediaType;

@Schema(description = "가이드 로그인 요청 DTO", 
        contentEncoding = MediaType.APPLICATION_JSON_VALUE)
@Data
public class GuideLoginRequestDto {
    private String username;
    private String password;
}