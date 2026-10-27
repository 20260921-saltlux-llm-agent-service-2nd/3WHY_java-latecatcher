package latecatcher.common;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JDBCTemplate {

    // DB 연결을 하나 만들어 돌려준다. 연결에 실패하면 null을 돌려주지 않고 IllegalStateException을 던진다.
    //
    // [변경 전] 실패하면 printStackTrace()로 출력만 하고 null을 돌려줬다.
    //  - 호출한 쪽은 실패했는지 알 수 없어서 con.prepareStatement(...)를 그대로 호출했고,
    //    여기서 원인과 상관없는 NullPointerException이 났다.
    //  - NullPointerException은 SQLException이 아니므로 저장소의 catch (SQLException)에 걸리지 않았다.
    //    IllegalStateException도 아니므로 Application.main의 catch에도 걸리지 않았다.
    //    결국 안내 메시지 없이 스택 트레이스를 출력하며 프로그램이 비정상 종료됐다.
    //  - 마지막에 보이는 오류가 "null에서 메서드를 호출했다"뿐이라,
    //    진짜 원인(비밀번호 오류, 서버 꺼짐 등)을 찾으려면 앞쪽 출력을 거슬러 올라가야 했다.
    //
    // [변경 후] 실패한 자리에서 바로 예외를 던진다.
    //  - 원래 예외를 두 번째 인자(cause)로 넘기므로 진짜 원인이 예외 체인에 그대로 남는다.
    //  - 저장소 코드는 고치지 않아도 된다. try-with-resources의 자원 선언 중에 예외가 나면
    //    con에 값이 들어가지 않으므로 닫을 자원이 없고, 예외는 catch (SQLException)를 지나 위로 올라간다.
    //  - Application.main의 catch (IllegalStateException)가 받아서
    //    여기서 넣은 메시지("DB에 연결하지 못했습니다." 등)와 원인 예외의 메시지를 화면에 출력한다.
    //
    // SQLException(검사 예외)이 아니라 IllegalStateException(비검사 예외)을 던지는 이유
    //  - 검사 예외를 던지면 이 메서드에 throws SQLException을 붙여야 한다. 그러면 try 밖에서
    //    getConnection()을 호출하는 MemberController.addCourse() 같은 곳이 컴파일 오류가 나서 함께 고쳐야 한다.
    //  - 저장소들이 이미 SQL 파일 오류와 DB 오류를 IllegalStateException으로 감싸서 올리고 있으므로 같은 규칙을 따른다.
    public static Connection getConnection() {

        Properties prop = new Properties();

        // [변경 전] prop.load(new FileReader(...))는 FileReader를 닫지 않아서,
        //  getConnection()을 호출할 때마다 닫히지 않은 파일 핸들이 하나씩 남았다.
        // [변경 후] FileReader를 try 괄호 안에 선언했다. 블록을 빠져나갈 때(return, 예외 모두) 자동으로 닫힌다.
        //
        // 주의: 괄호 안에 선언한 자원은 reader뿐이다.
        //  아래에서 return하는 Connection은 여기서 닫히지 않는다.
        //  호출한 저장소의 try-with-resources가 사용을 마친 뒤 닫는다.
        try (FileReader reader = new FileReader("src/main/resources/db.properties")) {
            prop.load(reader);

            String driver = prop.getProperty("driver");
            String url = prop.getProperty("url");

            Class.forName(driver);

            // [변경 전] con 변수에 담아 두었다가 try 밖에서 return했다. 실패하면 con이 null인 채로 return됐다.
            // [변경 후] 연결에 성공했을 때만 여기서 바로 return한다.
            //  실패하면 아래 catch 중 하나가 예외를 던지므로 null이 밖으로 나갈 경로가 없다.
            return DriverManager.getConnection(url, prop);

        } catch (IOException e) { // db.properties 파일이 없거나 읽을 수 없음
            throw new IllegalStateException("db.properties 파일을 읽지 못했습니다.", e);
        } catch (ClassNotFoundException e) { // driver에 적은 JDBC 드라이버 클래스가 없음 (의존성 누락, 클래스 이름 오타)
            throw new IllegalStateException("JDBC 드라이버를 찾지 못했습니다.", e);
        } catch (SQLException e) { // 서버 주소·포트, 계정, 비밀번호, DB 이름 문제 등으로 연결 실패
            throw new IllegalStateException("DB에 연결하지 못했습니다.", e);
        }
    }

    public static void close(Connection con) {

        try {
            // [변경 전] con != null & !con.isClosed()
            //  &는 앞 조건이 false여도 뒤 조건을 계산한다.
            //  그래서 con이 null이면 con.isClosed()를 호출해 NullPointerException이 났다.
            //  null 확인을 해 놓고도 null을 막지 못하는 코드였다.
            // [변경 후] &&는 앞 조건이 false면 뒤 조건을 계산하지 않는다(단락 평가).
            //  con이 null이면 isClosed()를 호출하지 않는다.
            //  아래 close(Statement), close(ResultSet)는 원래부터 &&를 쓰고 있었다.
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void close(Statement stmt) {

        try {
            if(stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void close(ResultSet rset) {

        try {
            if(rset != null && !rset.isClosed()) {
                rset.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
