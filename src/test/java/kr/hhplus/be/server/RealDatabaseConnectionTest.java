package kr.hhplus.be.server.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
class RealDatabaseConnectionTest {

    @Autowired
    DataSource dataSource;

    @Test
    void 실제_디비_연결정보를_출력한다() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ 연결된 DB URL: " + connection.getMetaData().getURL());
            System.out.println("✅ 연결된 DB 유저: " + connection.getMetaData().getUserName());
            System.out.println("✅ DB Product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("✅ DB Version: " + connection.getMetaData().getDatabaseProductVersion());
        }
    }
}
