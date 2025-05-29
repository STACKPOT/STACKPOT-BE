package stackpot.stackpot.chat.service.chat;

import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;

import java.util.concurrent.CompletableFuture;

public interface ChatFileService {

    ChatResponseDto.ChatFileDto saveFileInS3(final MultipartFile file);
}
