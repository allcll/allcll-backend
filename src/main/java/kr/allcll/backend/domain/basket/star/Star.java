package kr.allcll.backend.domain.basket.star;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.allcll.backend.domain.subject.Subject;
import kr.allcll.backend.support.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "stars")
@Entity
@Getter
@NoArgsConstructor
public class Star extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    public Star(String token, Subject subject) {
        this.token = token;
        this.subject = subject;
    }
}
