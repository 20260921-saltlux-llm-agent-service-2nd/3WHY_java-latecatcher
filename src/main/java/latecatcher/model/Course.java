package latecatcher.model;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Course {
    private int courseId;
    private String name; // 강의명
    private LocalDate startDate; // 강의 시작일
    private LocalDate endDate; // 강의 종료일
    private LocalDateTime startTime; // 수업 시작
    private LocalDateTime endTime; // 수업 종료
    private int partialPerAbsence; // 지각·조퇴·외출 n회 = 결석 1일
    private int allowanceThreshold; //장려금 기준 %
    private int completionThreshold; //수료 기준 %
}
