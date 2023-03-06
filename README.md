- 주제 : 돈 랜덤 뿌리기 REST API 기능 구현

## 1. 문제해결 전략
### 1-1) 뿌리기, 받기, 조회 기능을 수행하는 REST API를 구현
 - 프레임워크 : Spring Boot
 - 개발언어 : Java (OpenJDK14)
 - DBIO : Spring data JPA
 - 데이터베이스 : MariaDB
 - 유닛테스트 : jUnit
 - 기타 : lombok
 - 도구 : HeidiSQL, Postman

### 1-2) 다수의 서버에 다수의 인스턴스로 동작하더라도 기능에 문제가 없도록 설계
 - 단톡방에서 사용하는 카카오페이 뿌리기 기능의 경우 동시성제어와 관련한 충돌발생 가능성이 낮다고 판단
   > 수천 수백명이 모여있는 단톡방이 존재할 가능성과, 굉장히 근소한 차이로 트랜잭션을 발생시킬 가능성을 고려함
 - JPA에서 제공하는 Optimisstic Lock(낙관적 잠금) 기능을 사용하여 동시성제어를 실현함
 - 데브옵스 관점에서 API Gateway를통한 엔드포인트 단일화, RabbitMQ 분산락 등을 고려해봄
 - kafka는 큐 내부에서 라운드로빈 방식으로 분산처리를 하기때문에 순서를 보장해주지 못하므로 고려대상에서 제외
 
### 1-3) 각 기능 및 제약사항에 대한 단위테스트 작성
 - 제약사항에서 발생할수있는 시나리오를 구상하여 테스트케이스 작성
 - 예측할수없는 상황을 재현하고자 랜덤한 값을 임의로 발생시켜 스트레스 테스트 수행
 - 동시성제어(업데이트 유실) 테스트 수행

## 2. 기동환경 구축
 - IDE 설치 (이클립스, 인텔리제이 등)
 - JDK 8 이상 설치
 - MariaDB 또는 MySQL 설치
 - Database명 : kakaopay, 접속포트 : 3306
 - 테이블생성 : JPA Entity를 통한 자동생성 사용 (spring.jpa.hibernate.ddl-auto=create)

## 3. API 명세
### 3-1) 뿌리기 API
 - URL
   > POST /remittance/distribute
 - Header
   > X-USER-ID (Integer), X-ROOM-ID (String)
 - Body
   > {"amount":"0", "count":"0"}

### 3-2) 받기 API
 - URL
   > PUT /remittance/distribute/{:token}
 - Header
   > X-USER-ID (Integer), X-ROOM-ID (String)
 - Body
   > 없음
### 3-3) 조회 API
 - URL
   > GET /remittance/distribute/{:token}
 - Header
   > X-USER-ID (Integer), X-ROOM-ID (String)
 - Body
   > 없음
