package com.acme.training;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "mysql-verification")
class MysqlVerificationEnvironmentTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void verificationUsesRealMysqlEightAndRepeatableRead() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertEquals("MySQL", connection.getMetaData().getDatabaseProductName());
            assertTrue(connection.getMetaData().getDatabaseProductVersion().startsWith("8.0."));
            assertEquals(Connection.TRANSACTION_REPEATABLE_READ, connection.getTransactionIsolation());
            assertTrue(connection.getCatalog().startsWith("wms_verify_"));
        }
    }
}
