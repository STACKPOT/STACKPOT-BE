package stackpot.stackpot.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.code.ErrorReasonDTO;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4004", "등록된 사용자가 없습니다."),
    INVALID_MEMBER(HttpStatus.BAD_REQUEST, "MEMBER4005", "해당 팟의 멤버가 아닙니다."),

    // 유저 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4004", "유저를 찾을 수 없습니다."),
    USER_WITHDRAWAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "USER5001", "회원 탈퇴에 실패했습니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "USER4002", "이미 탈퇴한 사용자입니다."),
    LOGIN_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH4001", "로그인 티켓을 찾을 수 없습니다."),
    LOGIN_TICKET_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH4002", "로그인 티켓이 만료되었습니다."),
    LOGIN_TICKET_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5001", "로그인 티켓 처리 중 오류가 발생했습니다."),
    LOGIN_TICKET_SERIALIZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5001", "로그인 티켓 생성 중 오류가 발생했습니다."),
    LOGIN_TICKET_DESERIALIZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5002", "로그인 티켓 처리 중 오류가 발생했습니다."),

    // 인증 관련 에러
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4010", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH4030", "해당 리소스에 대한 접근 권한이 없습니다."),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4011", "유효하지 않은 인증 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "accessToken아 만료되었습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "refreshToken이 만료되었습니다."),

    // redis 관련 에러
    REDIS_CONNECTION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS5001", "Redis 서버에 연결할 수 없습니다."),
    REDIS_BLACKLIST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS5002", "블랙리스트 등록에 실패했습니다."),
    REDIS_AUTH_FAILURE(HttpStatus.UNAUTHORIZED, "REDIS4010", "Redis 인증에 실패했습니다."),
    REDIS_WRONG_TYPE(HttpStatus.BAD_REQUEST, "REDIS4001", "Redis 키의 타입이 올바르지 않습니다."),
    REDIS_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "REDIS4040", "Redis 키를 찾을 수 없습니다."),

    // Pot 관련 에러
    POT_NOT_FOUND(HttpStatus.NOT_FOUND, "POT4004", "팟이 존재하지 않습니다."),
    POT_FORBIDDEN(HttpStatus.FORBIDDEN, "POT4003", "팟 생성자가 아닙니다."),
    POT_OWNERSHIP_TRANSFER_REQUIRED(HttpStatus.CONFLICT,    "POT4005", "권한을 위임해 주세요."),

    // Pot Comment 관련 에러
    POT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POTCOMMENT4001", "Pot Comment를 찾을 수 없습니다"),

    // Pot 멤버 관련 에러
    POT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "POT_MEMBER4004", "해당 팟의 멤버가 아닙니다."),

    // 모집 관련 에러
    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT4004", "모집 내역이 없습니다."),

    // 지원 관련 에러
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION4004", "지원 내역이 없습니다."),
    DUPLICATE_APPLICATION(HttpStatus.BAD_REQUEST, "APPLICATION4001", "이미 해당 팟에 지원하셨습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH4030", "해당 팟 지원자 목록을 볼 수 있는 권한이 없습니다."),

    // 페이지 관련 에러
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "PAGE4000", "Page는 1이상입니다."),

    // 투두 관련 에러 코드
    USER_TODO_NOT_FOUND(HttpStatus.BAD_REQUEST, "TODO4004", "해당 Pot ID 및 Todo ID에 대한 투두를 찾을 수 없습니다."),
    USER_TODO_UNAUTHORIZED(HttpStatus.FORBIDDEN, "TODO4003", "해당 투두에 대한 수정 권한이 없습니다."),

    // Enum 관련 에러
    INVALID_POT_STATUS(HttpStatus.BAD_REQUEST, "POT_STATUS4000", "Pot Status 형식이 올바르지 않습니다 (RECRUITING / ONGOING / COMPLETED)"),
    INVALID_POT_MODE_OF_OPERATION(HttpStatus.BAD_REQUEST, "MODE_OF_OPERATION4000", "Pot ModeOfOperation 형식이 올바르지 않습니다 (ONLINE / OFFLINE / HYBRID)"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "ROLE4000", "Role 형식이 올바르지 않습니다 (FRONTEND / DESIGN / BACKEND / PLANNING)"),

    // Taskboard 관련 에러
    TASKBOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "TASKBOARD4004", "해당 Task를 찾을 수 없습니다."),
    TASKBOARD_POT_MISMATCH(HttpStatus.BAD_REQUEST, "TASKBOARD4005", "Task가 해당 Pot에 속하지 않습니다."),

    // Badge 관련 에러
    BADGE_NOT_FOUND(HttpStatus.NOT_FOUND, "BADGE4004", "해당 BADGE를 찾을 수 없습니다."),
    BADGE_INSUFFICIENT_TOP_MEMBERS(HttpStatus.BAD_REQUEST, "BADGE4001", "팀원이 1명 이하라 뱃지 수여 조건을 만족하지 않습니다. 팀원은 최소 2명 이상이어야 합니다."),
    BADGE_INSUFFICIENT_TODO_COUNTS(HttpStatus.BAD_REQUEST, "BADGE4002", "TODO를 완료한 사람이 2명 미만입니다. 최소 2명 이상이어야 합니다."),


    // 검색 관련 에러
    INVALID_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "SEARCH_STATUS4000", "검색 Type 형식이 올바르지 않습니다 (pot/feed)"),

    // feed 관연 에러
    FEED_UNAUTHORIZED(HttpStatus.FORBIDDEN, "FEED4003", "해당 Feed에 대한 수정 권한이 없습니다."),
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "FEED4004", "Feed를 찾을 수 없습니다."),

    // series 관련 에러
    SERIES_BAD_REQUEST(HttpStatus.BAD_REQUEST, "SERIES4001", "시리즈는 최대 5개까지만 등록할 수 있습니다."),
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "SERIES4002", "series를 찾을 수 없습니다."),


    // feed like 관련 에러
    FEED_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDLIKE4001", "Feed Like를 찾을 수 없습니다."),

    // feed comment 관련 에러
    FEED_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDCOMMENT4001", "Feed Comment를 찾을 수 없습니다"),

    // ChatRoom 관련 에러
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM4001", "팟의 채팅방이 없습니다"),
    CHATROOM_NOT_CHANGE(HttpStatus.NOT_FOUND, "CHATROOM4002", "채팅방 목록에 변화가 없습니다. 재요청 하세요"),

    // Chat 관련 에러
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT4001", "채팅이 없습니다"),
    CHAT_BAD_REQUEST(HttpStatus.BAD_REQUEST, "CHAT4002", "채팅 조회 파라미터 오류입니다"),

    // Notification 관련 에러
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "알림을 찾을 수 없습니다."),
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTIFICATION4002", "알림 타입이 올바르지 않습니다."),

    // AWS 관련 에러
    S3_BAD_REQUEST(HttpStatus.BAD_REQUEST, "AWS4001", "S3 저장 에러 발생");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
