package stackpot.stackpot.pot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import stackpot.stackpot.pot.entity.mapping.PotMember;
import stackpot.stackpot.pot.repository.PotMemberRepository;
import stackpot.stackpot.pot.repository.PotRepository;
import stackpot.stackpot.pot.repository.PotSaveRepository;
import stackpot.stackpot.search.dto.CursorPageResponse;
import stackpot.stackpot.user.entity.User;
import stackpot.stackpot.user.entity.enums.Role;
import stackpot.stackpot.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PotQueryServiceImpl implements PotQueryService {

    private final PotRepository potRepository;
    private final PotMemberRepository potMemberRepository;
    private final UserRepository userRepository;
    private final PotConverter potConverter;
    private final PotDetailConverter potDetailConverter;
    private final AuthService authService;
    private final MyPotConverter myPotConverter;
    private final PotSaveRepository potSaveRepository;

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
                            .map(entry -> RoleNameMapper.mapRoleName(entry.getKey()) + "(" + entry.getValue() + ")")
                            .collect(Collectors.joining(", "));

                    Role userPotRole = pot.getUser().getId().equals(user.getId())
                            ? pot.getUser().getRole()
                            : potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId()).orElse(pot.getUser().getRole());

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

        String recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(rd -> RoleNameMapper.mapRoleName(rd.getRecruitmentRole().name()) + "(" + rd.getRecruitmentCount() + ")")
                .collect(Collectors.joining(", "));

        return potDetailConverter.toPotDetailResponseDto(pot.getUser(), pot, recruitmentDetails, isOwner, isApplied);
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
        Page<Pot> potPage = potRepository.findByUserIdAndPotStatus(user.getId(), "RECRUITING", pageable);

        List<Pot> pots = potPage.getContent();

        List<Long> potIds = pots.stream()
                .map(Pot::getPotId)
                .collect(Collectors.toList());

        // 저장 수와 유저의 저장 여부를 한 번에 조회
        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);
        Set<Long> savedPotIds = potSaveRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds);

        List<PotPreviewResponseDto> content = pots.stream()
                .map(pot -> {
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    boolean isSaved = savedPotIds.contains(pot.getPotId());
                    int saveCount = potSaveCountMap.getOrDefault(pot.getPotId(), 0);

                    return potConverter.toPrviewDto(user, pot, roles, isSaved, saveCount);
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
    public List<OngoingPotResponseDto> getAppliedPots() {
        User user = authService.getCurrentUser();
        List<Pot> appliedPots = potRepository.findByPotApplication_User_Id(user.getId());
        // DTO 변환 시 userId 추가
        return appliedPots.stream()
                .map(pot -> myPotConverter.convertToOngoingPotResponseDto(pot, user.getId()))
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

        String summary = "(요약 결과 반환 로직 연결 예정)";

        return PotSummaryResponseDTO.builder().summary(summary).build();
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

                    Role userPotRole = pot.getUser().getId().equals(user.getId())
                            ? pot.getUser().getRole()
                            : potMemberRepository.findRoleByUserId(pot.getPotId(), user.getId()).orElse(pot.getUser().getRole());

                    return potConverter.toCompletedPotResponseDto(pot, formattedMembers, userPotRole);
                })
                .collect(Collectors.toList());

        return new CursorPageResponse<>(content, nextCursor, pots.size() > size);
    }

    @Override
    public CompletedPotDetailResponseDto getCompletedPotDetail(Long potId, Long userId) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new PotHandler(ErrorStatus.POT_NOT_FOUND));

        if (!"COMPLETED".equals(pot.getPotStatus())) {
            throw new PotHandler(ErrorStatus.INVALID_POT_STATUS);
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        PotMember potMember = potMemberRepository.findByPotAndUser(pot, targetUser)
                .orElse(null);

        String appealContent = (potMember != null) ? potMember.getAppealContent() : null;

        String userPotRole = pot.getUser().getId().equals(targetUser.getId())
                ? RoleNameMapper.mapRoleName(targetUser.getRole().name())
                : RoleNameMapper.mapRoleName(potMember.getRoleName().name());

        return potDetailConverter.toCompletedPotDetailDto(pot, userPotRole, appealContent);
    }
    @Override
    public Map<String, Object> getAllPotsWithPaging(Role role, int page, int size, Boolean onlyMine) {
        User user = null;
        if (onlyMine != null && onlyMine) {
            user = authService.getCurrentUser(); // 로그인 필수
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Pot> potPage;

        if (onlyMine != null && onlyMine) {
            potPage = potRepository.findByUserIdAndPotStatus(user.getId(), "RECRUITING", pageable);
        } else {
            potPage = (role == null)
                    ? potRepository.findAllOrderByApplicantsCountDesc(pageable)
                    : potRepository.findByRecruitmentRoleOrderByApplicantsCountDesc(role, pageable);
        }

        List<Pot> pots = potPage.getContent();
        List<Long> potIds = pots.stream().map(Pot::getPotId).collect(Collectors.toList());

        // 저장 수 및 저장 여부 일괄 조회
        Map<Long, Integer> potSaveCountMap = potSaveRepository.countSavesByPotIds(potIds);
        Set<Long> savedPotIds = (user != null)
                ? potSaveRepository.findPotIdsByUserIdAndPotIds(user.getId(), potIds)
                : Collections.emptySet();

        List<PotPreviewResponseDto> content = pots.stream()
                .map(pot -> {
                    List<String> roles = pot.getRecruitmentDetails().stream()
                            .map(rd -> String.valueOf(rd.getRecruitmentRole()))
                            .collect(Collectors.toList());

                    boolean isSaved = savedPotIds.contains(pot.getPotId());
                    int saveCount = potSaveCountMap.getOrDefault(pot.getPotId(), 0);

                    return potConverter.toPrviewDto(pot.getUser(), pot, roles, isSaved, saveCount);
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
}

