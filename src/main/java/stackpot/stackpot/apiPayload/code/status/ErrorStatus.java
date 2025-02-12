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
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4004", "등록된 사용자가 없습니다."),
    //유저 관련 에러
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH4010", "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH4030", "해당 리소스에 대한 접근 권한이 없습니다."),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4011", "유효하지 않은 인증 토큰입니다."),
    EXPIRED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4012", "인증 토큰이 만료되었습니다."),

    // Pot 관련 에러
    POT_NOT_FOUND(HttpStatus.NOT_FOUND, "POT4004", "팟이 존재하지 않습니다."),
    POT_FORBIDDEN(HttpStatus.FORBIDDEN, "POT4003", "팟 생성자가 아닙니다."),

    // Pot 멤버 관련 에러
    POT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "POT_MEMBER4004", "해당 팟의 멤버가 아닙니다."),

    // 모집 관련 에러
    RECRUITMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECRUITMENT4004", "모집 내역이 없습니다."),

    // 지원 관련 에러
    DUPLICATE_APPLICATION(HttpStatus.BAD_REQUEST, "APPLICATION4001", "이미 해당 팟에 지원하셨습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "AUTH4030", "해당 팟 지원자 목록을 볼 수 있는 권한이 없습니다."),

    // 페이지 관련 에러
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "PAGE4000", "Page는 1이상입니다."),

    // 투두 관련 에러 코드
    USER_TODO_NOT_FOUND(HttpStatus.BAD_REQUEST,"TODO4004", "해당 Pot ID 및 Todo ID에 대한 투두를 찾을 수 없습니다."),
    USER_TODO_UNAUTHORIZED(HttpStatus.FORBIDDEN,"TODO4003", "해당 투두에 대한 수정 권한이 없습니다."),

    // Enum 관련 에러
    INVALID_POT_STATUS(HttpStatus.BAD_REQUEST, "POT_STATUS4000", "Pot Status 형식이 올바르지 않습니다 (RECRUITING / ONGOING / COMPLETED)"),
    INVALID_POT_MODE_OF_OPERATION(HttpStatus.BAD_REQUEST, "MODE_OF_OPERATION4000", "Pot ModeOfOperation 형식이 올바르지 않습니다 (ONLINE / OFFLINE / HYBRID)"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "ROLE4000", "Role 형식이 올바르지 않습니다 (FRONTEND / DESIGN / BACKEND / PLANNING)");


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
