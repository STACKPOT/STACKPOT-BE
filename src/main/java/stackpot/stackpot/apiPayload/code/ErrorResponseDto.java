package stackpot.stackpot.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import stackpot.stackpot.apiPayload.code.status.ErrorStatus;


@Getter

public class ErrorResponseDto extends ErrorReasonDTO {

    private ErrorResponseDto(HttpStatus httpStatus, String code, String message) {
        super(httpStatus, false, code, message);
    }

    public static ErrorResponseDto of(ErrorStatus errorStatus) {
        return new ErrorResponseDto(
                errorStatus.getHttpStatus(),
                errorStatus.getCode(),
                errorStatus.getMessage()
        );
    }

    public static ErrorResponseDto of(ErrorStatus errorStatus, String message) {
        return new ErrorResponseDto(
                errorStatus.getHttpStatus(),
                errorStatus.getCode(),
                errorStatus.getMessage() + " - " + message
        );
    }

    public static ErrorResponseDto of(ErrorStatus errorStatus, Exception e) {
        return new ErrorResponseDto(
                errorStatus.getHttpStatus(),
                errorStatus.getCode(),
                errorStatus.getMessage() // 예외 메시지를 포함하려면 따로 가공 필요
        );
    }
}
