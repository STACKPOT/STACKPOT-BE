package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.ApplicantResponseDTO;
import stackpot.stackpot.web.dto.LikedApplicantResponseDTO;
import stackpot.stackpot.web.dto.MyPotResponseDTO;
import stackpot.stackpot.web.dto.PotAllResponseDTO;

import java.util.List;

public interface MyPotService {

    // 사용자의 진행 중인 팟 조회
    List<MyPotResponseDTO> getMyOnGoingPots();
}