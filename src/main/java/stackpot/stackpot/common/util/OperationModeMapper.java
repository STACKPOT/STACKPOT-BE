package stackpot.stackpot.common.util;

import java.util.Map;

public final class OperationModeMapper {

    private static final Map<String, String> modeMap = Map.of(
            "ONLINE", "온라인",
            "OFFLINE", "오프라인",
            "HYBRID", "혼합"
    );

    private OperationModeMapper() {} // 인스턴스화 방지

    public static String getKoreanMode(String modeOfOperation) {
        return modeMap.getOrDefault(modeOfOperation, "알 수 없음");
    }
}
