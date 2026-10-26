package latecatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

// 출결 1건 = 훈련생 1명의 하루. attendance_tbl 1행과 같다 (L-05, L-08, L-09)
// - 같은 훈련생·같은 날짜에는 1건만 있다 (UNIQUE(member_id, attend_date))
// - 상태(status)는 저장할 때 정해 둔다. 출석률 계산은 AttendanceRule이 status 개수로 한다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    // PK. 저장 전에는 0이고, DB가 AUTO_INCREMENT로 정한다
    private int attendanceId;

    // 훈련생 번호(member_tbl.member_id). 회원 탈퇴 시 출결도 함께 지워진다 (ON DELETE CASCADE)
    private int memberId;

    // 출결 날짜. 수업일(class_day_tbl)에 있는 날만 저장할 수 있다 (FK)
    private LocalDate attendDate;

    // 입실 시각. 결석이면 null
    private LocalTime checkIn;

    // 퇴실 시각. null이면 결석으로 판정한다 (퇴실 기록 없음 = 결석, PLAN 3절)
    // 입실보다 이를 수 없다 (CHECK check_out >= check_in)
    private LocalTime checkOut;

    // 출결 상태. 수동 입력(L-05)은 AttendanceRule.judge로 정하고, 고용24 가져오기(v0.2)는 받은 값 그대로 쓴다
    private AttendanceStatus status;

    // 메모. 지각이면서 조퇴인 날은 "조퇴"를 남긴다 (PLAN 3절 판정 순서)
    private String memo;

    // 입력 경로. "MANUAL"(콘솔 입력) 또는 "WORK24"(고용24 가져오기, v0.2). v0.1은 항상 "MANUAL"
    private String source;

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId=" + attendanceId +
                ", memberId=" + memberId +
                ", attendDate=" + attendDate +
                ", checkIn=" + checkIn +
                ", checkOut=" + checkOut +
                ", status=" + status +
                ", memo='" + memo + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
