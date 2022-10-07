# Project-SpringBoot-Board
게시판 -  자바 + 스프링부트와 관련 기술들을 공부
- IDE: IntelliJ IDEA 2022.2.1 (Community Edition)
- 언어 : Java 17
- 프레임워크 : Spring boot 2.7.0
- 빌드 도구 : gradle 7.4.1 (버전 중요하지 않음 - 스프링 부트 설정한 버전에 맞춰서 적합한 gradle 버전 선택됨)
- git GUI : GitKraken - git 형상 관리와 브랜치 전략 활용
- 각종 개발 전략과 도메인 설계, 실무 디자인 패턴, 비즈니스 로직의 구현을 경험

**강의에서 사용한 IntelliJ 추가 다운로드 플러그인**

- CamelCase(3.0.12)
- GitToolBox(212.9.0)
- JPA Buddy(2022.2.4-221)
- Key Promoter X(2022.1.2)
- Presentation Assistant(1.0.9)
- Ideolog(203.0.30.0)
- Spring Boot Assistant(0.14.0)

**프로젝트에서 사용한 IntelliJ 추가 다운로드 플러그인(색상/테마)**

- ANSI Highlighter(1.2.4) → 이후 유료
- Atom Material Icons(64.0.0)
- Grep Console(12.12.211.6693.0)
- One Dark theme(5.6.0)

### 테스트와 배포 - 고객에게 제품을 보여주고 성과를 확인하는 순간

- 테스트
    - 개발 요구사항이 빠짐 없이 모두 구현되었는가(일이 끝났는가)
    - 구현된 요구사항이 오류 없이 동작하는가(일이 잘 끝났는가)
    - JUnit 5.8.2
    - 각종 테스트 라이브러리(Mockito, AssertJ등)
    - 스프링 부트 슬라이스 테스트 테크닉
    - 깃헙: 테스트/빌드 자동화
- 배포
    - 깃헙 릴리즈 작성
    - 클라우드 서버에 배포(Heroku) — 무료  AWS, Azure — 유료
        - 최근 보안 이슈로 일부 자동화 기능을 이용하지 못 할 수 있음(토큰 유출)
        - Heroku를 사용하지 못할 경우, 로컬에서 실행
    - 깃헙 : Heroku 배포 자동화
    
       
    
- Reference
    - [https://https://www.diagrams.net/](https://www.diagrams.net/)
    - [https://www.heroku.com/](https://www.heroku.com/)
    - [https://junit.org/junit5/](https://junit.org/junit5/)
    - [https://site.mockito.org/](https://site.mockito.org/)
    - [https://assertj.github.io/doc/](https://assertj.github.io/doc/)
