# Java 주문 서버 예제

Spring Boot 기반의 주문 서버 예제입니다.

구현한 기능:

- 휴대폰 앱이 주문을 생성하는 API
- 관리자가 주문을 조회하고 상태를 변경하는 API
- 주문 상태 흐름 `WAITING -> DELIVERING -> COMPLETED`
- 관리자가 직접 주문하면 로봇 수신 모듈로 주문 내역 자동 전송
- 로봇으로 전송된 내역을 조회하는 API

## 기술 스택

- Java 17
- Spring Boot 3
- Maven
- 인메모리 저장소

## 실행

Java 17, Maven이 설치되어 있다면 아래 명령으로 실행할 수 있습니다.

```bash
mvn spring-boot:run
```

기본 포트는 `8080`입니다.

## API 요약

### 1. 앱에서 주문 생성

`POST /api/orders`

```json
{
  "customerName": "홍길동",
  "phoneNumber": "010-1111-2222",
  "deliveryAddress": "서울시 강남구 테헤란로 1",
  "items": [
    {
      "name": "아메리카노",
      "quantity": 2,
      "unitPrice": 4500
    }
  ]
}
```

### 2. 관리자가 주문 생성

`POST /api/admin/orders`

관리자가 주문하면 주문 생성과 동시에 로봇 수신함으로 내역이 전송됩니다.

### 3. 관리자 주문 목록 조회

`GET /api/admin/orders`

### 4. 주문 상태 변경

`PATCH /api/admin/orders/{orderId}/status`

```json
{
  "status": "DELIVERING"
}
```

이후 `COMPLETED`로 변경할 수 있습니다.

### 5. 로봇 전송 내역 조회

`GET /api/robot/dispatches`

## 설계 포인트

- `MOBILE_APP`, `ADMIN`으로 주문 생성 주체를 구분했습니다.
- 관리자 주문은 `RobotGateway`를 통해 로봇 시스템으로 전달되도록 분리했습니다.
- 현재 로봇 연동은 예제용 인메모리 구현이며, 실제 환경에서는 메시지 큐나 로봇 제어 API로 교체할 수 있습니다.
