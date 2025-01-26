package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class EnumHandler extends GeneralException {
    public EnumHandler(BaseErrorCode code) {
        super(code);
    }
}
