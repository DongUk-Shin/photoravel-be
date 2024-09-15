package trendravel.photoravel_be.domain.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trendravel.photoravel_be.commom.error.MemberErrorCode;
import trendravel.photoravel_be.commom.exception.ApiException;
import trendravel.photoravel_be.db.inmemorydb.entity.Token;
import trendravel.photoravel_be.db.member.MemberEntity;
import trendravel.photoravel_be.db.respository.member.MemberRepository;
import trendravel.photoravel_be.domain.authentication.session.UserSession;
import trendravel.photoravel_be.domain.member.convertor.MemberConvertor;
import trendravel.photoravel_be.domain.member.dto.*;
import trendravel.photoravel_be.domain.token.model.TokenDto;
import trendravel.photoravel_be.domain.token.model.TokenResponse;
import trendravel.photoravel_be.domain.token.service.TokenService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberConvertor memberConvertor;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Token> redisTemplate;

    @Transactional
    public TokenResponse login(BaseMemberDto baseMemberDto) {
        MemberEntity member = memberRepository.findByEmail(baseMemberDto.getEmail())
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
        return issueTokenResponse(member);
    }


    @Transactional
    public TokenResponse addInfoWithLogin(CompleteMemberDto memberDto) {

        MemberEntity memberEntity = MemberEntity.builder()
                .memberId(memberDto.getProvider() + "_" + memberDto.getId())
                .password(passwordEncoder.encode(memberDto.getProvider() + "_" + memberDto.getId() + "key"))
                .email(memberDto.getEmail())
                .name(memberDto.getName())
                .nickname(memberDto.getNickname())
                .profileImg(memberDto.getProfileImg())
                .build();

        MemberEntity saved = memberRepository.save(memberEntity);
        log.info("saved member : {}", saved.getEmail());

        return issueTokenResponse(saved);
    }

    @Transactional
    public MemberResponse localRegister(MemberRegisterRequest request) {
        MemberEntity memberEntity = MemberEntity.builder()
                .memberId(request.getMemberId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .profileImg(request.getProfileImg())
                .build();
        MemberEntity saved = memberRepository.save(memberEntity);

        return memberConvertor.toMemberResponse(saved);
    }

    @Transactional
    public MemberResponse getMemberInfo(String memberId) {

        MemberEntity memberEntity = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));

        return memberConvertor.toMemberResponse(memberEntity);
    }

    @Transactional
    public MemberResponse memberModi(MemberModRequest request) {

        // 스프링 시큐리티 컨텍스트 홀더에서 인증 객체를 찾음으로써 인증된 사용자인지 검증
        // jwt 토큰 인증 필터를 거칠 때 홀더에 인증 객체를 저장하기 때문.

        UserSession principal = (UserSession) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberEntity memberEntity = memberRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ApiException(MemberErrorCode.UNAUTHORIZED));

        memberEntity.setMemberId(request.getMemberId());
        memberEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        memberEntity.setName(request.getName());
        memberEntity.setNickname(request.getNickname());
        memberEntity.setProfileImg(request.getProfileImg());

        MemberEntity saved = memberRepository.save(memberEntity);

        return memberConvertor.toMemberResponse(saved);
    }

    @Transactional
    public MemberInfoCheckResponse emailCheck(String email) {

        boolean result = memberRepository.findByEmail(email).isPresent();

        return MemberInfoCheckResponse.builder()
                .isDuplicated(result)
                .build();
    }

    @Transactional
    public MemberInfoCheckResponse nicknameCheck(String nickname) {
        boolean result = memberRepository.findByNickname(nickname).isPresent();

        return MemberInfoCheckResponse.builder()
                .isDuplicated(result)
                .build();
    }

    @Transactional
    public MemberInfoCheckResponse memberIdCheck(String memberId) {
        boolean result = memberRepository.findByMemberId(memberId).isPresent();

        return MemberInfoCheckResponse.builder()
                .isDuplicated(result)
                .build();
    }

    @Transactional
    public void delete(String memberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserSession principal = (UserSession) authentication.getPrincipal();

        MemberEntity member = memberRepository.findByMemberId(principal.getUsername())
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
        log.info("find member : {}, memberId : {}", member.getMemberId(), memberId);

        if (member.getMemberId().equals(memberId)){
            redisTemplate.opsForHash().delete("refresh_token", memberId);
            SecurityContextHolder.clearContext();
            memberRepository.delete(member);
        }else {
            throw new ApiException(MemberErrorCode.UNAUTHORIZED);
        }
    }

    @Transactional
    public TokenResponse localLogin(MemberLoginRequest request) {
        MemberEntity memberEntity = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new ApiException(MemberErrorCode.MEMBER_ID_NOT_MATCH));
        if (passwordEncoder.matches(request.getPassword(), memberEntity.getPassword())) {
            return issueTokenResponse(memberEntity);
        } else {
            throw new ApiException(MemberErrorCode.PASSWORD_NOT_MATCH);
        }
    }


    private TokenResponse issueTokenResponse(MemberEntity member) {
        TokenDto accessTokenDto = tokenService.issueAccessToken(member.getEmail());
        TokenDto refreshTokenDto = tokenService.issueRefreshToken(member.getEmail());

        return TokenResponse.builder()
                .accessToken(accessTokenDto)
                .refreshToken(refreshTokenDto)
                .build();
    }
}