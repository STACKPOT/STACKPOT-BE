package stackpot.stackpot.apiPayload.exception.handler;

import stackpot.stackpot.apiPayload.code.BaseErrorCode;
import stackpot.stackpot.apiPayload.exception.GeneralException;

public class RecruitmentHandler extends GeneralException {
    public RecruitmentHandler(BaseErrorCode code) {
        super(code);
    }
}
