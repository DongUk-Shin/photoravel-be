package trendravel.photoravel_be.domain.guide.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trendravel.photoravel_be.commom.response.Api;
import trendravel.photoravel_be.commom.response.Result;
import trendravel.photoravel_be.domain.guide.dto.request.GuideLoginRequestDto;
import trendravel.photoravel_be.domain.guide.dto.request.GuideRequestDto;
import trendravel.photoravel_be.domain.guide.dto.response.GuideResponseDto;
import trendravel.photoravel_be.domain.guide.service.GuideService;

import java.util.List;

@RestController
@RequestMapping("/guides")
@RequiredArgsConstructor
public class GuideController {
    
    private final GuideService guideService;
    
    @Schema(description = "가이드 회원 가입(CREATE) 요청",
            contentEncoding = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/join")
    public Result createGuide(
            @RequestPart(value = "data") GuideRequestDto guideRequestDto,
            @RequestPart(value = "image", required = false) String image) {
        
        guideService.createGuide(guideRequestDto, image);
        
        return Result.CREATED();
    }
    
    @Schema(description = "가이드 로그인 요청",
            contentEncoding = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/login")
    public Result guideLogin(@RequestBody GuideLoginRequestDto loginRequestDto) {
        
        guideService.authenticate(loginRequestDto.getUsername(),
                loginRequestDto.getPassword());
        
        return Result.OK();
    }
    
    @Schema(description = "가이드 목록 READ 요청",
            contentEncoding = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping
    public Api<List<GuideResponseDto>> guidesList(@RequestParam String keyword) {
        
        return Api.READ(guideService.getGuideList(keyword));
    }
    
    @Schema(description = "가이드 상세 정보 READ 요청")
    @GetMapping("/{guideId}/detail")
    public Api<GuideResponseDto> guideDetail(@PathVariable String guideId) {
        
        return Api.READ(guideService.getGuide(guideId));
    }
    
    
    @Schema(description = "가이드 정보 UPDATE 요청",
            contentEncoding = MediaType.APPLICATION_JSON_VALUE)
    @PatchMapping("/{guideId}/update")
    public Api<GuideResponseDto> updateGuide(
            @PathVariable String guideId,
            @RequestPart(value = "data") GuideRequestDto guideRequestDto,
            @RequestPart(value = "image", required = false) List<MultipartFile> images) {
        
        return Api.UPDATED(guideService.updateGuide(guideId, guideRequestDto, images));
    }
    
    @Schema(description = "가이드 정보 DELETE 요청",
            contentEncoding = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/{guideId}/delete")
    public Result guideDelete(@PathVariable String guideId) {
        
        guideService.deleteGuide(guideId);
        return Result.DELETED();
    }
    
}
