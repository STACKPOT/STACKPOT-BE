package stackpot.stackpot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stackpot.stackpot.converter.UserConverter;
import stackpot.stackpot.domain.User;
import stackpot.stackpot.repository.UserRepository.UserRepository;
import stackpot.stackpot.web.dto.UserRequestDto;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService{

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User joinUser(UserRequestDto.JoinDto request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        User addUser = UserConverter.toUser(request);
        updateUserData(user, request);

        return userRepository.save(user);
    }

    private void updateUserData(User user, UserRequestDto.JoinDto request) {
//        if (addUser.getKakaoId() != null) {
//            user.setKakaoId(addUser.getKakaoId());
//        }
//        if(addUser.getNickname() != null){
//            user.setNickname(addUser.getNickname());
//        }
//        if(addUser.getRole() != null){
//            user.setRole(addUser.getRole());
//        }
//        if(addUser.getInterest() != null){
//            user.setInterest(addUser.getInterest());
//        }
        if (request.getKakaoId() != null) {
            user.setKakaoId(request.getKakaoId());
            System.out.println("request.getKakaoId() : " + request.getKakaoId());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getInterest() != null) {
            user.setInterest(request.getInterest());
        }
    }
}
