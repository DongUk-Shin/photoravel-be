package trendravel.photoravel_be.db.review;


import jakarta.persistence.*;
import lombok.*;
import trendravel.photoravel_be.db.BaseEntity;
import trendravel.photoravel_be.db.member.MemberEntity;
import trendravel.photoravel_be.db.photographer.Photographer;
import trendravel.photoravel_be.db.location.Location;
import trendravel.photoravel_be.db.spot.Spot;
import trendravel.photoravel_be.domain.review.dto.request.ReviewRequestDto;
import trendravel.photoravel_be.db.review.enums.ReviewTypes;
import trendravel.photoravel_be.domain.review.dto.request.ReviewUpdateImagesDto;

import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "REVIEW")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewTypes reviewType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    @Column(nullable = false)
    private Double rating;

    @ElementCollection
    @CollectionTable(
            name = "review_images",
            joinColumns = @JoinColumn(name = "review_id")
    )
    private List<String> images;

    // 회원, 가이드 관련 연관관계 필드 추가 필요


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location locationReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private Spot spotReview;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographerReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberEntity_id")
    private MemberEntity member;
    
    //연관관계 편의 메소드
    public void setSpotReview(Spot spot) {
        this.spotReview = spot;
        spot.getReviews().add(this);
    }

    public void setLocationReview(Location location) {
        this.locationReview = location;
        location.getReview().add(this);
    }

    public void setMemberReview(MemberEntity member){
        this.member = member;
        member.getReviewMember().add(this);
    }
    
    public void setPhotographerReview(Photographer photographer) {
        this.photographerReview = photographer;
        photographer.getReviews().add(this);
    }

    public void updateReview(ReviewUpdateImagesDto review, List<String> newImages) {
        this.content = review.getContent();
        this.rating = review.getRating();
        if(review.getDeleteImages() != null){
            for (String deleteImage : review.getDeleteImages()) {
                this.images.remove(deleteImage);
            }
        }
        if(!newImages.isEmpty()){
            this.images.addAll(newImages);
        }
    }

    public void updateReview(ReviewUpdateImagesDto review) {
        this.content = review.getContent();
        this.rating = review.getRating();
        if(review.getDeleteImages() != null){
            for (String deleteImage : review.getDeleteImages()) {
                this.images.remove(deleteImage);
            }
        }
    }

}
