package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class AwsHandler extends GeneralException {
    public AwsHandler(BaseErrorCode code) {
        super(code);
    }
}
