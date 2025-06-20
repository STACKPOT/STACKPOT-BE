package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class PotCommentHandler extends GeneralException {
    public PotCommentHandler(BaseErrorCode code) {
        super(code);
    }
}
