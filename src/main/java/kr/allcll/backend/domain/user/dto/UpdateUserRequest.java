package kr.allcll.backend.domain.user.dto;

import kr.allcll.backend.domain.graduation.MajorType;

public record UpdateUserRequest(
    MajorType majorType,
    String deptNm,
    String doubleDeptNm
) {

}
