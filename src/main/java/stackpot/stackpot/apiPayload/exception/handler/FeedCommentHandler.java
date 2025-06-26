package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class FeedCommentHandler extends GeneralException {
    public FeedCommentHandler(BaseErrorCode code) {
        super(code);
    }
}
