package kr.allcll.backend.domain.user.dto;

public record LoginRequest(
    String studentId,
    String password
) {

}
