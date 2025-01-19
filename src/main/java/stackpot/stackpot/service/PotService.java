package stackpot.stackpot.service;

import stackpot.stackpot.web.dto.PotResponseDTO;

import java.util.List;

public interface PotService {
    List<PotResponseDTO> getAllPots(String role);
    PotResponseDTO getPotDetails(Long potId);
}