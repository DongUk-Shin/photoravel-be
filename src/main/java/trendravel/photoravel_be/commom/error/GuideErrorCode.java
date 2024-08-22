package trendravel.photoravel_be.commom.error;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GuideErrorCode implements ErrorCodeIfs{
    
    GUIDE_NOT_FOUND(404, "가이드북을 찾을 수 없음");


    private final Integer httpStatusCode;
    private final String errorDescription;

}
