package stackpot.stackpot.feed.entity.enums;


public enum Interest {
    SIDE_PROJECT("사이드 프로젝트"),
    SOLO_DEVELOPMENT("1인 개발"),
    COMPETITION("공모전"),
    STARTUP("창업"),
    NETWORKING("네트워킹 행사");

    private final String label;

    Interest(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
