package stackpot.stackpot.common.util;

import java.util.Map;

public final class RoleNameMapper {

    private static final Map<String, String> roleMap = Map.of(
            "BACKEND", "양파",
            "FRONTEND", "버섯",
            "DESIGN", "브로콜리",
            "PLANNING", "당근"
    );

    private RoleNameMapper() {} // 인스턴스화 방지

    public static String mapRoleName(String potRole) {
        return roleMap.getOrDefault(potRole, "멤버");
    }
}