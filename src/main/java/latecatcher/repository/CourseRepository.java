package latecatcher.repository;

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
     * 3. 과정 전체 조회 (L-08 내 출결 리스크용)
     * - 과정 번호로 과정 설정값을 모두 조회
     * - selectCoursePeriod는 시작일·종료일만 채워서, 기준값이 필요한 L-08은 이 메서드를 쓴다
     * - 해당 과정이 없으면 null 반환
     */
    public Course selectCourse(Connection con, int courseId) {
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        Course course = null;

        // XML에서 쿼리 꺼내기
        String query = prop.getProperty("selectCourse");

        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, courseId);

            rset = pstmt.executeQuery();

            if (rset.next()) {
                course = new Course(
                        rset.getInt("course_id"),
                        rset.getString("name"),
                        rset.getDate("start_date").toLocalDate(),
                        rset.getDate("end_date").toLocalDate(),
                        rset.getTime("start_time").toLocalTime(),
                        rset.getTime("end_time").toLocalTime(),
                        rset.getInt("partial_per_absence"),  // 지각·조퇴·외출 n회 = 결석 1일
                        rset.getInt("min_attend_minutes"),   // 최소 수업 시간(분)
                        rset.getInt("allowance_threshold"),  // 장려금 기준 % (80% 경고 기준)
                        rset.getInt("completion_threshold")  // 수료 기준 %
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rset);
            close(pstmt);
        }

        return course;
    }
}
