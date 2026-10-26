package latecatcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ClassDay {
    private LocalDate classDate; // 날짜
    private boolean classDay;    // 수업일 여부
    private String note;         // 메모

    @Override
    public String toString() {
        return "ClassDay{" +
                "classDate=" + classDate +
                ", classDay=" + classDay +
                ", note='" + note + '\'' +
                '}';
    }
}
