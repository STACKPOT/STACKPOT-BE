package stackpot.stackpot.pot.service.potComment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stackpot.stackpot.common.util.AuthService;
import stackpot.stackpot.pot.converter.PotCommentConverter;
import stackpot.stackpot.pot.dto.PotCommentDto;
import stackpot.stackpot.pot.dto.PotCommentResponseDto;
import stackpot.stackpot.pot.entity.mapping.PotComment;
import stackpot.stackpot.pot.repository.PotCommentRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PotCommentQueryService {

    private final PotCommentRepository potCommentRepository;
    private final PotCommentConverter potCommentConverter;
    private final AuthService authService;

    public PotComment selectPotCommentByCommentId(Long commentId) {
        return potCommentRepository.findByCommentId(commentId).orElse(null);
    }

    public List<PotCommentResponseDto.AllPotCommentDto> selectAllPotComments(Long potId) {
        List<PotCommentResponseDto.AllPotCommentDto> result = new ArrayList<>();
        List<PotCommentDto.PotCommentInfoDto> dtos = potCommentRepository.findAllCommentInfoDtoByPotId(potId);
        dtos.forEach(dto -> result.add(potCommentConverter.toAllPotCommentDto(dto)));
        return result;
    }
}
