package skeleton.model;

import lombok.*;

// 구독 서비스 사용자 정보
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    private int memberId;
    private String name;
    private int age;
    private String phone;

    @Override
    public String toString() {
        return "Member{" +
               "memberId=" + memberId +
               ", name='" + name + '\'' +
               ", age=" + age +
               ", phone='" + phone + '\'' +
               '}';
    }
}
