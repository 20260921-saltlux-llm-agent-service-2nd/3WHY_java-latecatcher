package latecatcher.model;

// 출결 상태. 한글 라벨과 환산 횟수를 가진다 (L-05, L-08, L-09)
// - 상수 이름은 attendance_tbl.status ENUM과 철자까지 같아야 한다. 그래야 변환 코드 없이 주고받는다
//   읽기: AttendanceStatus.valueOf(resultSet.getString("status")) / 저장: status.name()
// - 출석률 계산(AttendanceRule)에서 상태별로 다르게 계산한다 (PLAN 3절)
//   환산 결석 = 결석 + floor((지각 + 조퇴 + 외출) / partialPerAbsence)
//   출석률   = (기록 일수 − 공결 − 환산 결석) / (기록 일수 − 공결) × 100
public enum AttendanceStatus {

    PRESENT("출석", 0),
    LATE("지각", 1),
    EARLY_LEAVE("조퇴", 1),

    // 지각·조퇴. 최소 수업 시간(4시간)을 채우고, 지각도 조퇴도 했다. 환산 횟수에 2회로 포함한다 (10/26 결정)
    LATE_EARLY_LEAVE("지각·조퇴", 2),

    // 외출. 입실·퇴실 시각에 남지 않고 매니저·강사에게 사후에 서류로 처리한다
    // 그래서 judge로 판정하지 않고 v0.1에는 입력 경로가 없다 (계산에 0회로 들어감)
    // 고용24 가져오기(v0.2)에서 받은 값으로만 들어오고, 그때는 지각·조퇴처럼 환산한다
    OUTING("외출", 1),
    ABSENT("결석", 0),
    EXCUSED("공결", 0) // 공결(인정 결석). 시각으로 판정하지 않는다. 출석률 분모·분자에서 모두 뺀다
    ;

    // 화면에 보여 줄 한글 이름 (예: "10/06 지각으로 저장했습니다")
    private final String label;

    // 이 상태 하루가 지각·조퇴·외출 환산 횟수에 포함되는 수 (Course.partialPerAbsence회 = 결석 1일)
    // 지각·조퇴·외출 1, 지각·조퇴 2, 출석·결석·공결 0. 결석은 환산 없이 1일로 계산하고, 공결은 계산에서 빠진다
    private final int partialCount;

    AttendanceStatus(String label, int partialCount) {
        this.label = label;
        this.partialCount = partialCount;
    }

    public String getLabel() {
        return label;
    }

    // 환산 횟수. L-08·L-09가 지각·조퇴·외출 합계를 구할 때 더한다 (partial += status.getPartialCount())
    public int getPartialCount() {
        return partialCount;
    }

    // 환산 대상이면 true (환산 횟수가 1 이상)
    public boolean isPartial() {
        return partialCount > 0;
    }
}
