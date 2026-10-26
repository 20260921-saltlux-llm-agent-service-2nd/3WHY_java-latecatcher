package latecatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private int courseId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int partialPerAbsence;
    private int minAttendMinutes = 240; // 최소 수업 시간(분). course_tbl.min_attend_minutes, 기본 240(4시간). 미만이면 결석
    private int allowanceThreshold;
    private int completionThreshold;

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", partialPerAbsence=" + partialPerAbsence +
                ", minAttendMinutes=" + minAttendMinutes +
                ", allowanceThreshold=" + allowanceThreshold +
                ", completionThreshold=" + completionThreshold +
                '}';
    }
}
