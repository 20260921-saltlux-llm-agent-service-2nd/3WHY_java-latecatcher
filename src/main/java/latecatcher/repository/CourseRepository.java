package latecatcher.repository;

import latecatcher.common.JDBCTemplate;
import latecatcher.model.Course;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static latecatcher.common.JDBCTemplate.close;

public class CourseRepository {

    private final Properties prop = new Properties();

    public CourseRepository() {
        try {
            // 프로젝트 내 XML 쿼리 파일 경로
            prop.loadFromXML(new FileInputStream("src/main/java/latecatcher/mapper/course-query.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 1. 과정 등록 (L-03)
     */
    public int insertCourse(Connection con, Course course) {
        PreparedStatement pstmt = null;
        int result = 0;

        String query = prop.getProperty("insertCourse");

        try {
            pstmt = con.prepareStatement(query);

            pstmt.setInt(1, course.getCourseId());
            pstmt.setString(2, course.getName());
            pstmt.setDate(3, Date.valueOf(course.getStartDate()));
            pstmt.setDate(4, Date.valueOf(course.getEndDate()));
            pstmt.setTime(5, Time.valueOf(course.getStartTime()));
            pstmt.setTime(6, Time.valueOf(course.getEndTime()));
            pstmt.setInt(7, course.getPartialPerAbsence());
            pstmt.setInt(8, course.getAllowanceThreshold());
            pstmt.setInt(9, course.getCompletionThreshold());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(pstmt);
        }

        return result;
    }

    /*
     * 2. 과정 기간 조회 (L-04 수업일 생성용)
     * - 과정 번호로 시작일·종료일을 조회
     * - 해당 과정이 없으면 null 반환
     */
    public Course selectCoursePeriod(Connection con, int courseId) {
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        Course course = null;

        // XML에서 쿼리 꺼내기
        String query = prop.getProperty("selectCoursePeriod");

        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, courseId);

            rset = pstmt.executeQuery();

            if (rset.next()) {
                course = new Course();
                course.setCourseId(courseId);
                course.setStartDate(rset.getDate("start_date").toLocalDate());
                course.setEndDate(rset.getDate("end_date").toLocalDate());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rset);
            close(pstmt);
        }

        return course;
    }

    /*
     * 2. 과정 조회 (L-05 출결 판정, L-08) — YY 추가
     * 가장 오래된 과정(가장 작은 course_id) 1행을 돌려준다. 과정이 없으면 null
     * 여러 과정 등록·조회는 v0.1 범위 밖(확장 기능)이라, 과정이 여러 개여도 1번 과정을 쓴다
     * 확장할 때는 회원이 course_id를 갖게 하고 findCourse(courseId)로 바꾼다 (erd-extended.md)
     */
    public Course findCourse() {
        String sql = prop.getProperty("selectCourse");

        try (Connection con = JDBCTemplate.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return new Course(
                        rs.getInt("course_id"),
                        rs.getString("name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime(),
                        rs.getInt("partial_per_absence"),
                        rs.getInt("min_attend_minutes"),
                        rs.getInt("allowance_threshold"),
                        rs.getInt("completion_threshold")
                );
            }
            return null;

        } catch (SQLException e) {
            throw new IllegalStateException("과정 조회 중 오류가 발생했습니다.", e);
        }
    }

}
