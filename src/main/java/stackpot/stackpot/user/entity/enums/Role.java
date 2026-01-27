package stackpot.stackpot.user.entity.enums;

import lombok.Getter;

@Getter
public enum Role {
    BACKEND("양파", "백엔드"),
    FRONTEND("버섯", "프론트엔드"),
    DESIGN("브로콜리", "디자인"),
    PLANNING("당근", "기획"),
    DEFAULT("새싹", "새싹"),
    UNKNOWN("UNKNOWN", "알 수 없음");

    private final String vegetable;
    private final String koreanName;

    Role(String vegetable, String koreanName) {
        this.vegetable = vegetable;
        this.koreanName = koreanName;
    }

    public static Role fromString(String roleName) {
        try {
            return Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public static String toVegetable(String roleName) {
        return fromString(roleName).getVegetable();
    }

    public static String toKoreanName(String roleName) {
        return fromString(roleName).getKoreanName();
    }
}