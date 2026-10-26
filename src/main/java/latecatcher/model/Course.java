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
    private LocalTime startTime; // 수업 시작 (course_tbl.start_time TIME)
    private LocalTime endTime; // 수업 종료 (course_tbl.end_time TIME)
    private int partialPerAbsence; // 지각·조퇴·외출 n회 = 결석 1일
    private int allowanceThreshold; //장려금 기준 %
    private int completionThreshold; //수료 기준 %
}
