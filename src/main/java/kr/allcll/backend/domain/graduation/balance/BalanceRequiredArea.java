package kr.allcll.backend.domain.graduation.balance;

import lombok.Getter;

@Getter
public enum BalanceRequiredArea {
    HISTORY_THOUGHT("역사와사상"),
    NATURE_SCIENCE("자연과학"),
    ECONOMY_SOCIETY("경제와사회"),
    CULTURE_ARTS("문화와예술"),
    CONVERGENCE_AND_CREATIVITY("융합과창의");

    private final String name;

    BalanceRequiredArea(String name) {
        this.name = name;
    }

    //엑셀의 선택영역 문자열을 BalanceRequiredArea로 변환
    public static BalanceRequiredArea fromSelectedArea(String selectedArea) {
        if (selectedArea == null || selectedArea.trim().isEmpty()) {
            return null;
        }

        String normalizedInput = selectedArea.trim().replace(" ", "");

        for (BalanceRequiredArea area : values()) {
            if (area.name.equals(normalizedInput)) {
                return area;
            }
        }
        return null;
    }
}
