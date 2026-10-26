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
}
