package trendravel.photoravel_be.domain.guidebook.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import trendravel.photoravel_be.domain.guidebook.dto.request.GuidebookRequestDto;
import trendravel.photoravel_be.domain.guidebook.dto.response.GuidebookResponseDto;
import trendravel.photoravel_be.db.guidebook.Guidebook;
import trendravel.photoravel_be.db.enums.Region;
import trendravel.photoravel_be.db.respository.guidebook.GuidebookRepository;
import trendravel.photoravel_be.commom.service.ImageService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuidebookService {
    
    private final GuidebookRepository guidebookRepository;
    private final ImageService imageService;
    
    @Transactional
    public GuidebookResponseDto createGuidebook(
            GuidebookRequestDto guidebookRequestDto, 
            List<MultipartFile> images) {
        
        Guidebook guidebook = guidebookRepository.save(Guidebook.builder()
                .userId(guidebookRequestDto.getUserId())
                .title(guidebookRequestDto.getTitle())
                .content(guidebookRequestDto.getContent())
                .region(guidebookRequestDto.getRegion())
                .images(imageService.uploadImages(images))
                .views(0)
                .build());
        
        
        return GuidebookResponseDto.builder()
                .id(guidebook.getId())
                .userId(guidebook.getUserId())
                .title(guidebook.getTitle())
                .content(guidebook.getContent())
                .region(guidebook.getRegion())
                .images(guidebook.getImages())
                .createdAt(guidebook.getCreatedAt())
                .updatedAt(guidebook.getUpdatedAt())
                .views(guidebook.getViews())
                .build();
    }
    
    @Transactional
    public GuidebookResponseDto createGuidebook(
            GuidebookRequestDto guidebookRequestDto) {
        
        Guidebook guidebook = guidebookRepository.save(Guidebook.builder()
                .userId(guidebookRequestDto.getUserId())
                .title(guidebookRequestDto.getTitle())
                .content(guidebookRequestDto.getContent())
                .region(guidebookRequestDto.getRegion())
                .views(0)
                .build());
        
        return GuidebookResponseDto.builder()
                .id(guidebook.getId())
                .userId(guidebook.getUserId())
                .title(guidebook.getTitle())
                .content(guidebook.getContent())
                .region(guidebook.getRegion())
                .createdAt(guidebook.getCreatedAt())
                .updatedAt(guidebook.getUpdatedAt())
                .views(guidebook.getViews())
                .build();
    }
    
    
    @Transactional
    public List<GuidebookResponseDto> getGuidebookList(String region, String keyword) {
        
        List<Guidebook> guidebooks;
        
        if (keyword == null || keyword.isEmpty()) {
            guidebooks = guidebookRepository.findByRegion(Region.valueOf(region));
        } else {
            guidebooks = guidebookRepository.findByTitleContaining(keyword);
        }
        
        List<GuidebookResponseDto> responseDtoList = new ArrayList<>();
        
        for (Guidebook guidebook : guidebooks) {
            GuidebookResponseDto dto = GuidebookResponseDto.builder()
                    .id(guidebook.getId())
                    .userId(guidebook.getUserId())
                    .title(guidebook.getTitle())
                    .region(guidebook.getRegion())
                    .views(guidebook.getViews())
                    .createdAt(guidebook.getCreatedAt())
                    .updatedAt(guidebook.getUpdatedAt())
                    .build();
            responseDtoList.add(dto);
        }
        
        return responseDtoList;
    }
    
    @Transactional
    public GuidebookResponseDto getGuidebook(Long guidebookId) {
        Guidebook guidebook = guidebookRepository.findById(guidebookId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Guidebook ID: " 
                                + guidebookId + " does not exist"));
        
        guidebook.increaseView();
        
        return GuidebookResponseDto.builder()
                .userId(guidebook.getUserId())
                .title(guidebook.getTitle())
                .content(guidebook.getContent())
                .region(guidebook.getRegion())
                .views(guidebook.getViews())
                .images(guidebook.getImages())
                .createdAt(guidebook.getCreatedAt())
                .updatedAt(guidebook.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public GuidebookResponseDto updateGuidebook(GuidebookRequestDto guidebookRequestDto,
                                     List<MultipartFile> images) {
        
        Optional<Guidebook> guidebookOpt = 
                guidebookRepository.findById(guidebookRequestDto.getId());
        if (guidebookOpt.isEmpty()) {
            // 검색 실패
        }
        
        Guidebook guidebook = guidebookOpt.get();
        guidebook.updateGuidebook(guidebookRequestDto, imageService.uploadImages(images));
        
        return GuidebookResponseDto.builder()
                .id(guidebook.getId())
                .userId(guidebook.getUserId())
                .title(guidebook.getTitle())
                .content(guidebook.getContent())
                .region(guidebook.getRegion())
                .views(guidebook.getViews())
                .images(guidebook.getImages())
                .createdAt(guidebook.getCreatedAt())
                .updatedAt(guidebook.getUpdatedAt())
                .build();
    }
    
    @Transactional
    public GuidebookResponseDto updateGuidebook(GuidebookRequestDto guidebookRequestDto) {
        
        Optional<Guidebook> guidebookOpt = 
                guidebookRepository.findById(guidebookRequestDto.getId());
        
        if (guidebookOpt.isEmpty()) {
            // 검색 실패
        }
        
        Guidebook guidebook = guidebookOpt.get();
        guidebook.updateGuidebook(guidebookRequestDto);
        
        return GuidebookResponseDto.builder()
                .id(guidebook.getId())
                .userId(guidebook.getUserId())
                .title(guidebook.getTitle())
                .content(guidebook.getContent())
                .region(guidebook.getRegion())
                .views(guidebook.getViews())
                .createdAt(guidebook.getCreatedAt())
                .updatedAt(guidebook.getUpdatedAt())
                .build();
    }
    
    
    
    public void deleteGuidebook(Long guidebookId) {
        guidebookRepository.deleteById(guidebookId);
    }
    
    
}