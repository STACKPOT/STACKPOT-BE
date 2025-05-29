package stackpot.stackpot.chat.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;
import stackpot.stackpot.apiPayload.exception.handler.AwsHandler;
import stackpot.stackpot.aws.s3.AmazonS3Manager;
import stackpot.stackpot.chat.converter.ChatConverter;
import stackpot.stackpot.chat.dto.response.ChatResponseDto;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ChatFileServiceImpl implements ChatFileService {

    private final ChatConverter chatConverter;
    private final AmazonS3Manager amazonS3Manager;

    // TODO 비동기
    @Override
    public ChatResponseDto.ChatFileDto saveFileInS3(final MultipartFile file) {
        String keyName = "chat/file/" + UUID.randomUUID();
        String fileUrl = amazonS3Manager.uploadFile(keyName, file);
        return chatConverter.toChatFileDto(fileUrl);
    }
}
