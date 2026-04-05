# MJ-Maker (마이클 잭슨 합치기 게임)

## 과제 제출 항목
* 프로젝트 제목: MJ Maker
* 발표 영상 자료 링크: [유튜브](https://youtu.be/MQ3Ev7C5HKw)
* 프로젝트 Git Repository 링크: [레포지토리](https://github.com/limdev59/MJ-Maker)
* README.md 링크: [README.md](https://github.com/limdev59/MJ-Maker/edit/master/README.md)

---

## 1. 게임 컨셉

### High Concept
마이클 잭슨의 연대기별 사진 객체를 낙하시켜 동일한 사진끼리 합치며 진화시키는 물리 기반 머지 퍼즐 게임

### 핵심 메카닉
* 터치 입력을 통한 객체 투하 위치 결정 및 낙하
* 동일한 종류의 사진 객체가 충돌 시 상위 단계 객체로 교체되는 머지 시스템
* a2dg 프레임워크의 World 레이어 시스템을 활용한 객체 적재 및 렌더링

---

## 2. 개발 범위

이번 학기 수업에서 다룬 프레임워크의 적용

### 장면 관리 (Scene Management)
* SceneStack을 활용하여 총 3종의 장면(TitleScene, MainScene, ResultScene) 관리
* 각 장면 전환 시 onEnter, onExit 등 생명주기 API 연동

### 객체 및 월드 관리 (World & Objects)
* World 클래스의 제네릭 레이어를 활용하여 4개 레이어(Background, Stall, Current, UI) 운용
* Sprite 클래스를 확장한 7단계의 MJ 사진 객체 구현
* BitmapPool을 통한 비트맵 리소스 캐싱 및 관리

### 시스템 및 물리
* GameMetrics를 활용한 가상 좌표계 시스템 적용 및 화면 해상도 독립적 설계
* 객체 삭제 및 생성 시 발생하는 ConcurrentModificationException 해결
* 원형 충돌 판정 알고리즘을 통한 머지 로직 처리

---

## 3. 예상 게임 실행 흐름
![수박게임 로비](https://github.com/user-attachments/assets/de5f5496-ef46-49f3-8960-fd270bc2a14b)
<img width="320" height="180" alt="수박게임" src="https://github.com/user-attachments/assets/37cf5a67-37df-4324-9f25-d6b4a157b13f" />
<img width="384" height="688" alt="Gemini_Generated_Image_25vg5n25vg5n25vg" src="https://github.com/user-attachments/assets/088a40ca-e6fd-472d-94d8-47089a8742db" />

* 타이틀 화면: 게임 제목과 시작 버튼 배치. push()를 통해 게임 장면으로 진입.
* 게임 화면 (투하): 화면 상단에서 터치한 위치로 객체가 생성되어 자유 낙하.
* 게임 화면 (머지): 바닥에 쌓인 동일 사진 충돌 시 삭제 및 상위 단계 객체 생성.
* 게임 종료: 객체가 상단 제한선을 넘을 시 change()를 통해 결과 화면으로 이동.

---

## 4. 개발 일정 (8주)

4월 6일 시작 주를 1주차로 설정한 상세 일정입니다.

| 주차 | 기간 | 상세 개발 내용 |
| :--- | :--- | :--- |
| 1주차 | 04/06 - 04/12 | 프로젝트 초기화 및 프레임워크 구조 구축 |
| 2주차 | 04/13 - 04/19 | SceneStack 및 기본 장면 클래스 구조 설계 |
| 3주차 | 04/20 - 04/26 | GameMetrics 가상 좌표계 설정 및 BitmapPool 연동 |
| 4주차 | 04/27 - 05/03 | 터치 이벤트 처리 및 객체 낙하 기초 물리 구현 |
| 5주차 | 05/04 - 05/10 | World 레이어 시스템을 활용한 객체 적재 관리 |
| 6주차 | 05/11 - 05/17 | 동일 객체 충돌 판정 및 머지(Merge) 시스템 완성 |
| 7주차 | 05/18 - 05/24 | 예외 처리(ConcurrentModification) 및 점수 시스템 구현 |
| 8주차 | 05/25 - 05/31 | 최종 기능 점검 및 1분 30초 발표 영상 제작 |

---

## 5. 구현 체크리스트

- [ ] a2dg 프레임워크 모듈 분리 및 의존성 설정
- [ ] SceneStack 기반 장면 전환 및 생명주기 관리
- [ ] 가상 좌표계 기반 Sprite 렌더링 및 BitmapPool 적용
- [ ] World 제네릭 레이어를 이용한 객체 순서 제어
- [ ] 터치 위치에 따른 객체 생성 및 물리 낙하 구현
- [ ] 객체 간 충돌 감지 및 머지 로직 구현
- [ ] 객체 삭제 시스템 예외 처리 완료
- [ ] 7단계 진화 단계 및 결과 출력 시스템 구현
