package stackpot.stackpot.chat.service.chat;

import stackpot.stackpot.chat.dto.request.ChatRequestDto;
import stackpot.stackpot.chat.entity.Chat;

public interface ChatCommandService {

    Chat saveChatMessage(final ChatRequestDto.ChatMessageDto chatMessageDto);
}
