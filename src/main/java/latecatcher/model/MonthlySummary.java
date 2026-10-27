package latecatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 훈련생 1명의 한 달 출결 집계. v_monthly_attendance 뷰 1행과 같다 (L-09)
// - SQL은 상태별 개수만 구하고, 출석률·미입력 계산은 AttendanceRule이 한다
// - 지각·조퇴(LATE_EARLY_LEAVE)인 날은 lateCnt와 earlyCnt에 모두 들어 있다. 그래서 개수의 합은 기록 일수가 아니다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummary {

    // 훈련생 번호(member_tbl.member_id)
    private int memberId;

    // 훈련생 이름
    private String name;

    // 조회 월. "2026-10" 형식 (DATE_FORMAT(attend_date, '%Y-%m'))
    private String ym;

    // 기록 일수 = 그 달 출결 기록 수. 출석률 계산의 기준
    private int recordCnt;

    private int presentCnt;  // 출석
    private int lateCnt;     // 지각 (지각·조퇴 포함)
    private int earlyCnt;    // 조퇴 (지각·조퇴 포함)
    private int outingCnt;   // 외출
    private int absentCnt;   // 결석
    private int excusedCnt;  // 공결. 출석률 분모·분자에서 모두 뺀다

    // 지각·조퇴·외출 환산 횟수 합. 지각·조퇴인 날이 lateCnt·earlyCnt에 모두 들어 있어 2회로 세진다
    // (AttendanceStatus.LATE_EARLY_LEAVE의 환산 횟수 2와 같다)
    public int getPartialCnt() {
        return lateCnt + earlyCnt + outingCnt;
    }

    @Override
    public String toString() {
        return "MonthlySummary{" +
                "memberId=" + memberId +
                ", name='" + name + '\'' +
                ", ym='" + ym + '\'' +
                ", recordCnt=" + recordCnt +
                ", presentCnt=" + presentCnt +
                ", lateCnt=" + lateCnt +
                ", earlyCnt=" + earlyCnt +
                ", outingCnt=" + outingCnt +
                ", absentCnt=" + absentCnt +
                ", excusedCnt=" + excusedCnt +
                '}';
    }
}
