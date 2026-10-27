package latecatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.OptionalDouble;

// 반 전체 현황 순위표 한 줄 = 훈련생 1명 (L-09)
// - DB에서 가져온 월별 집계(MonthlySummary)에 Java가 계산한 값(순위·출석률·미입력)을 붙인 것이다
// - AttendanceController가 만들고, AttendanceView는 받은 값을 그대로 보여 주기만 한다
@Getter
@AllArgsConstructor
public class ClassStatusRow {

    // 순위. 출석률이 같으면 같은 순위다 (100%, 100%, 90% → 1, 1, 3)
    // 출석률을 계산할 수 없는 사람(공결만 있음)은 순위를 매기지 않아 0이다
    private int rank;

    // 뷰에서 가져온 그 달 집계 (이름, 기록 일수, 상태별 개수)
    private MonthlySummary summary;

    // 출석률 %. 공결만 있어 분모가 0이면 empty (AttendanceRule.attendanceRate)
    private OptionalDouble rate;

    // 미입력 일수 = 조회 기간 수업일 − 기록 일수. 계산에는 넣지 않고 화면에만 보여 준다 (PLAN 3절)
    private int missingDays;

    // 경계 = 장려금 기준 이상, 기준 + 5%p 이하 (우리 과정은 80% 이상 85% 이하, 이슈 #11 정할 것 4)
    private boolean border;

    // 미달 = 출석률 < 장려금 기준. HAVING 미달 명단(findBelowThreshold)과 같은 사람이어야 한다
    private boolean below;
}
