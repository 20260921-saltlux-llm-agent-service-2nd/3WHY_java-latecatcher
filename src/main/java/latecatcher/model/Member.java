package latecatcher.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private int memberId;
    private String name;
    private int age;
    private String phone;

    // ③ 회원가입용 생성자 (member_id는 AUTO_INCREMENT라 제외)
    public Member(String name, int age, String phone) {
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

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
