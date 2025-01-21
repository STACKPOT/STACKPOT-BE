package stackpot.stackpot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.config.security.JwtTokenProvider;
import stackpot.stackpot.converter.MyPotTodoConverter;
import stackpot.stackpot.domain.Pot;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.domain.mapping.PotApplication;
import stackpot.stackpot.domain.mapping.UserTodo;
import stackpot.stackpot.repository.PotRepository.MyPotRepository;
import stackpot.stackpot.repository.PotRepository.PotRepository;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPotServiceImpl implements MyPotService {

    private final PotRepository potRepository;
    private final MyPotRepository myPotRepository;
    private final UserRepository userRepository;
    private final MyPotTodoConverter myPotTodoConverter;


    @Override
    public List<MyPotResponseDTO> getMyOnGoingPots() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 사용자가 만든 팟 조회
        List<Pot> myPots = potRepository.findByUserId(user.getId());

        // 진행 중인 팟 리스트 변환 (멤버 정보 포함)
        List<MyPotResponseDTO.OngoingPotsDetail> ongoingPots = myPots.stream()
                .filter(pot -> "진행중".equals(pot.getPotStatus()))
                .map(this::convertToOngoingPotDetail)
                .collect(Collectors.toList());

        // MyPotResponseDTO로 변환하여 반환
        return List.of(MyPotResponseDTO.builder()
                .ongoingPots(ongoingPots)
                .build());
    }

    /*@Override
    public MyPotTodoResponseDTO postTodo(Long potId, MyPotTodoRequestDTO requestDTO) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 Pot 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // To-Do 생성
        UserTodo userTodo = UserTodo.builder()
                .pot(pot)
                .user(user)
                .content(requestDTO.getContent())
                .status(requestDTO.getStatus())
                .build();

        System.out.println("Received potId from path: " + potId);
        System.out.println("Request content: " + requestDTO.getContent());
        System.out.println("Request status: " + requestDTO.getStatus());

        myPotRepository.save(userTodo);

        return myPotTodoConverter.toTodoResultDto(userTodo);
    }*/

    @Override
    public MyPotTodoResponseDTO postTodo(Long potId, MyPotTodoRequestDTO requestDTO) {
        // 현재 인증된 사용자 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        // 해당 Pot 존재 여부 확인
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with id: " + potId));

        // 사용자가 pot의 멤버인지 확인
        boolean isMember = pot.getPotMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of the requested pot.");
        }

        // To-Do 생성
        UserTodo userTodo = UserTodo.builder()
                .pot(pot)
                .user(user)
                .content(requestDTO.getContent())
                .status(requestDTO.getStatus())
                .build();

        System.out.println("Received potId from path: " + potId);
        System.out.println("Request content: " + requestDTO.getContent());
        System.out.println("Request status: " + requestDTO.getStatus());

        myPotRepository.save(userTodo);

        return myPotTodoConverter.toTodoResultDto(userTodo);
    }

    // 진행 중인 팟 변환 메서드 (멤버 포함)
    private MyPotResponseDTO.OngoingPotsDetail convertToOngoingPotDetail(Pot pot) {
        List<RecruitmentDetailsResponseDTO> recruitmentDetails = pot.getRecruitmentDetails().stream()
                .map(details -> RecruitmentDetailsResponseDTO.builder()
                        .recruitmentId(details.getRecruitmentId())
                        .recruitmentRole(details.getRecruitmentRole())
                        .recruitmentCount(details.getRecruitmentCount())
                        .build())
                .collect(Collectors.toList());

        List<PotMemberResponseDTO> potMembers = pot.getPotMembers().stream()
                .map(member -> PotMemberResponseDTO.builder()
                        .potMemberId(member.getPotMemberId())
                        .roleName(member.getRoleName())

                        .build())
                .collect(Collectors.toList());

        return MyPotResponseDTO.OngoingPotsDetail.builder()
                .user(UserResponseDTO.builder()
                        .nickname(pot.getUser().getNickname())
                        .role(pot.getUser().getRole())
                        .build())
                .pot(PotResponseDto.builder()
                        .potName(pot.getPotName())
                        .potStartDate(pot.getPotStartDate())
                        .potEndDate(pot.getPotEndDate())
                        .potStatus(pot.getPotStatus())
                        .build())
                .recruitmentDetails(recruitmentDetails)
                .potMembers(potMembers)
                .build();
    }



}