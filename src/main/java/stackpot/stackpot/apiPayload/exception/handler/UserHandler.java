package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode code) {
        super(code);
    }
}
