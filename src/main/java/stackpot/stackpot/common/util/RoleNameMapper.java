package stackpot.stackpot.common.util;

import java.util.Map;

public final class RoleNameMapper {

    private static final Map<String, String> roleMap = Map.of(
            "BACKEND", "양파",
            "FRONTEND", "버섯",
            "DESIGN", "브로콜리",
            "PLANNING", "당근"
    );

    public static String getKoreanRoleName(String role) {
        Map<String, String> roleToKoreanMap = Map.of(
                "BACKEND", "백엔드",
                "FRONTEND", "프론트엔드",
                "DESIGN", "디자인",
                "PLANNING", "기획"
        );
        return roleToKoreanMap.getOrDefault(role, "알 수 없음");
    }

    private RoleNameMapper() {} // 인스턴스화 방지

    public static String mapRoleName(String potRole) {
        return roleMap.getOrDefault(potRole, "멤버");
    }
}