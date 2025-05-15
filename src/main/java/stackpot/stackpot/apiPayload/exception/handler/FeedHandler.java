package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class FeedHandler extends GeneralException {
    public FeedHandler(BaseErrorCode code) {
        super(code);
    }
}
