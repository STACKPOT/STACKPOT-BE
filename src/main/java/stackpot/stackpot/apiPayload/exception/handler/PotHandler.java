package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class PotHandler extends GeneralException {
    public PotHandler(BaseErrorCode code) {
        super(code);
    }
}
