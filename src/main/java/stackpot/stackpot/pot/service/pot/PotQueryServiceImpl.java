package stackpot.stackpot.pot.service.pot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.MemberHandler;
import stackpot.stackpot.apiPayload.exception.handler.PotHandler;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.common.util.RoleNameMapper;
import stackpot.stackpot.pot.converter.MyPotConverter;
import stackpot.stackpot.pot.converter.PotConverter;
import stackpot.stackpot.pot.converter.PotDetailConverter;
import stackpot.stackpot.pot.dto.*;
import stackpot.stackpot.pot.entity.Pot;
import stackpot.stackpot.pot.entity.mapping.PotApplication;
import stackpot.stackpot.pot.repository.PotCommentRepository;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.search.dto.CursorPageResponse;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PotQueryServiceImpl implements PotQueryService {

    private final PotCommentRepository potCommentRepository;
    private final PotRepository potRepository;
    private final PotMemberRepository potMemberRepository;
    private final UserRepository userRepository;
    private final PotConverter potConverter;
    private final PotDetailConverter potDetailConverter;
    private final AuthService authService;
    private final MyPotConverter myPotConverter;
    private final PotSaveRepository potSaveRepository;
    private final PotSummarizationService potSummarizationService;

    @Override
    public CursorPageResponse<CompletedPotResponseDto> getMyCompletedPots(Long cursor, int size) {
        User user = authService.getCurrentUser();
        List<Pot> pots = potRepository.findCompletedPotsCreatedByUser(user.getId(), cursor);

        List<Pot> result = pots.size() > size ? pots.subList(0, size) : pots;
        Long nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getPotId();

        List<CompletedPotResponseDto> content = result.stream()
                .map(pot -> {
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> RoleNameMapper.getKoreanRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Role userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                            .orElse(null);

                    return potConverter.toCompletedPotResponseDto(pot, formattedMembers, userPotRole);
                })
                .collect(Collectors.toList());

        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

    @Override
    public PotDetailResponseDto getPotDetails(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findPotWithRecruitmentDetailsByPotId(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        boolean isOwner = pot.getUser().getId().equals(user.getId());
        boolean isApplied = pot.getPotApplication().stream()
                .anyMatch(application -> application.getUser().getId().equals(user.getId()));

        boolean isSaved = potSaveRepository.existsByUserAndPot_PotId(user, potId);

        String recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(rd -> RoleNameMapper.getKoreanRoleName(rd.getRecruitmentRole().name()) + "(" + rd.getRecruitmentCount() + ")")
                .collect(Collectors.joining(", "));

        Long countComment = potCommentRepository.countByPotId(potId);
        Role creatorRole = potMemberRepository
                .findRoleByUserId(pot.getPotId(), pot.getUser().getId())
                .orElse(null);

        String creatorRoleName = creatorRole != null ? creatorRole.name() : "UNKNOWN";
        return potDetailConverter.toPotDetailResponseDto(pot.getUser(), pot, recruitmentDetails, isOwner, isApplied, isSaved, countComment, creatorRoleName);
    }

    @Override
    public List<LikedApplicantResponseDTO> getLikedApplicants(Long potId) {
        User user = authService.getCurrentUser();
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!pot.getUser().getId().equals(user.getId())) {
            throw new PotHandler(ErrorStatus.POT_FORBIDDEN);
        }

        return pot.getPotApplication().stream()
                .filter(PotApplication::getLiked)
                .map(app -> LikedApplicantResponseDTO.builder()
                        .applicationId(app.getApplicationId())
                        .applicantRole(app.getPotRole())
                        .potNickname(app.getUser().getNickname() + RoleNameMapper.mapRoleName(app.getPotRole().name()))
                        .liked(app.getLiked())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMyRecruitingPotsWithPaging(Integer page, Integer size) {
        User user = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Pot> potPage = potRepository.findByUserIdAndPotStatusOrderByCreatedAtDesc(user.getId(), "RECRUITING", pageable);

        List<Pot> pots = potPage.getContent();

        List<Long> potIds = pots.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        Set<Long> memberPotIds = (user != null && !potIds.isEmpty())
                ? potMemberRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        // 저장 수와 유저의 저장 여부를 한 번에 조회
        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);
        Set<Long> savedPotIds = potSaveRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds);

        List<PotPreviewResponseDto> content = pots.stream()
                .map(pot -> {
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    Long potId = pot.getPotId();
                    boolean isSaved = savedPotIds.contains(potId);
                    int saveCount = potSaveCountMap.getOrDefault(potId, 0);

                    boolean isMember = "COMPLETED".equals(pot.getPotStatus()) && memberPotIds.contains(potId);

                    return potConverter.toPrviewDto(user, pot, roles, isSaved, saveCount, isMember);
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("pots", content);
        result.put("currentPage", potPage.getNumber() + 1);
        result.put("totalPages", potPage.getTotalPages());
        result.put("totalElements", potPage.getTotalElements());
        result.put("size", potPage.getSize());

        return result;
    }

    @Override
    public List<AppliedPotResponseDto> getAppliedPots() {
        User user = authService.getCurrentUser();
        List<Pot> appliedPots = potRepository.findByPotApplication_User_Id(user.getId());
        return appliedPots.stream()
                .map(pot -> {
                    // 역할 목록 추출
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    // 역할을 포함하여 DTO 생성
                    return myPotConverter.convertToAppliedPotResponseDto(pot, roles);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PotSummaryResponseDTO getPotSummary(Long potId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        String prompt = "우리 프로젝트를 포트폴리오에 적합한 방식으로 400자로 요약해줘.\n" +
                "1. 프로젝트 개요: 해결하려는 문제, 목표\n" +
                "2. 주요 기능: 핵심적인 기능 설명\n" +
                "3. 기술 스택: 사용한 언어 및 프레임워크\n" +
                "4. 운영 방식: 온라인/오프라인 여부 및 협업 방식\n" +
                "5. 포트폴리오 적합성: 실무 경험, 팀워크, 기술 스택 습득 등의 강점 부각\n\n" +
                "프로젝트 정보:\n" +
                "- 프로젝트명: " + pot.getPotName() + "\n" +
                "- 내용: " + pot.getPotContent() + "\n" +
                "- 사용 기술: " + pot.getPotLan() + "\n" +
                "- 운영 방식: " + pot.getPotModeOfOperation();

        String summary = potSummarizationService.summarizeText(prompt, 400);

        return PotSummaryResponseDTO.builder()
                .summary(summary)
                .build();
    }

    @Override
    public CursorPageResponse<CompletedPotResponseDto> getUserCompletedPots(Long userId, Long cursor, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Pot> pots = potRepository.findCompletedPotsByCursor(user.getId(), cursor);

        List<Pot> result = pots.size() > size ? pots.subList(0, size) : pots;
        Long nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getPotId();

        List<CompletedPotResponseDto> content = result.stream()
                .map(pot -> {
                    List<Object[]> roleCounts = potMemberRepository.findRoleCountsByPotId(pot.getPotId());
                    Map<String, Integer> roleCountsMap = roleCounts.stream()
                            .collect(Collectors.toMap(
                                    roleCount -> ((Role) roleCount[0]).name(),
                                    roleCount -> ((Long) roleCount[1]).intValue()
                            ));
                    String formattedMembers = roleCountsMap.entrySet().stream()
                            .map(entry -> RoleNameMapper.mapRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Role userPotRole = potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId())
                            .orElse(null); // or throw if required

                    return potConverter.toCompletedPotResponseDto(pot, formattedMembers, userPotRole);
                })
                .collect(Collectors.toList());

        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

    @Override
    public Map<String, Object> getAllPotsWithPaging(List<Role> roles, int page, int size, Boolean onlyMine) {
        User user = null;

        // 로그인 여부와 무관하게 인증된 사용자인 경우 user 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();

        if (isAuthenticated) {
            user = authService.getCurrentUser();
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Pot> potPage;

        if (onlyMine != null && onlyMine && user != null) {
            potPage = potRepository.findByUserIdAndPotStatusOrderByCreatedAtDesc(user.getId(), "RECRUITING", pageable);
        } else {
            if (roles == null || roles.isEmpty()) {
                potPage = potRepository.findAllOrderByApplicantsCountDesc("RECRUITING", pageable);
            } else {
                potPage = potRepository.findByRecruitmentRolesInOrderByApplicantsCountDesc(roles, "RECRUITING", pageable);
            }
        }

        List<Pot> pots = potPage.getContent();
        List<Long> potIds = pots.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);

        Set<Long> savedPotIds = (user != null && !potIds.isEmpty())
                ? potSaveRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        Set<Long> memberPotIds = (user != null && !potIds.isEmpty())
                ? potMemberRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        List<PotPreviewResponseDto> content = pots.stream()
                .map(pot -> {
                    Long potId = pot.getPotId();
                    List<String> potRoles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    boolean isSaved = savedPotIds.contains(potId);
                    int saveCount = potSaveCountMap.getOrDefault(potId, 0);
                    boolean isMember = memberPotIds.contains(potId);

                    return potConverter.toPrviewDto(pot.getUser(), pot, potRoles, isSaved, saveCount, isMember);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("pots", content);
        response.put("currentPage", potPage.getNumber() + 1);
        response.put("totalPages", potPage.getTotalPages());
        response.put("totalElements", potPage.getTotalElements());
        response.put("size", potPage.getSize());

        return response;
    }
    @Override
    public Pot getPotByPotId(Long potId) {
        return potRepository.findById(potId).orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));
    }
}

