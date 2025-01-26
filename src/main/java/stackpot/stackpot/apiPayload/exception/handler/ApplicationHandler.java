package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class ApplicationHandler extends GeneralException {
    public ApplicationHandler(BaseErrorCode code) {
        super(code);
    }
}
