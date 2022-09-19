package com.spring.projectboard.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Table(indexes = {

        @Index(columnList = "title"),
        @Index(columnList = "hashtag"),
        @Index(columnList = "createdAt"),
        @Index(columnList = "createdBy")
})
@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter @Column(nullable = false) private String title; // 제목
    @Setter @Column(nullable = false, length = 10000) private String content; //본문

    @Setter private String hashtag;

    @CreatedDate @Column(nullable = false)  private LocalDateTime createdAt; //생성일시
    @CreatedBy @Column(nullable = false, length = 100) private String createdBy; //생성자
    @LastModifiedDate private LocalDateTime modifiedAt; //수저일시
    @LastModifiedBy private String modifiedBy; //수정자
}
