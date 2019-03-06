package com.github.senin24.dbtests;

import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class DeliveryLockImpl {

    private String giud;
    private String name;
    private int time;
    private JdbcTemplate jdbcTemplate;

    private static final Logger log = getLogger(DeliveryLockImpl.class);

    private static final String TABLE_NAME = "DELIVERY_LOCK";

    private static final String SET_LOCK =
            "MERGE INTO " + TABLE_NAME + " dest " +
                    "USING ( " +
                    "   SELECT " +
                    "       ? LOCK_GUID, " +
                    "       ? LOCK_NAME, " +
                    "       SYSDATE CREATE_DATE, " +
                    "       SYSDATE + (1/24/60/60) * ? TIMEOUT_DATE " +
                    "   FROM dual) source " +
                    "ON (source.LOCK_NAME = dest.LOCK_NAME) " +
                    "WHEN NOT MATCHED THEN " +
                    "   INSERT (dest.LOCK_GUID, dest.LOCK_NAME, dest.CREATE_DATE, dest.TIMEOUT_DATE) " +
                    "   VALUES (source.LOCK_GUID, source.LOCK_NAME, source.CREATE_DATE, source.TIMEOUT_DATE) " +
                    "WHEN MATCHED THEN " +
                    "  UPDATE SET " +
                    "    dest.LOCK_GUID = source.LOCK_GUID, " +
                    "    dest.CREATE_DATE = source.CREATE_DATE, " +
                    "    dest.TIMEOUT_DATE = source.TIMEOUT_DATE " +
                    "  WHERE source.CREATE_DATE > dest.TIMEOUT_DATE";

    private static final String SET_LOCK2 =
            "MERGE INTO " + TABLE_NAME + " AS dest " +
                    "USING ( " +
                        "SELECT " +
                            "'edb53502-31cc-459c-9ac8-acf8c51de643' LOCK_GUID, " +
                            "'11' LOCK_NAME, " +
                            "SYSDATE CREATE_DATE, " +
                            "SYSDATE + (1/24/60/60) * 10 TIMEOUT_DATE " +
                        "FROM dual) source " +
                    "ON (source.LOCK_NAME = dest.LOCK_NAME) " +
                    "WHEN NOT MATCHED THEN " +
                        "INSERT (dest.LOCK_GUID, dest.LOCK_NAME, dest.CREATE_DATE, dest.TIMEOUT_DATE) " +
                        "VALUES (source.LOCK_GUID, source.LOCK_NAME, source.CREATE_DATE, source.TIMEOUT_DATE) " +
                    "WHEN MATCHED THEN " +
                        "UPDATE SET " +
                            "dest.LOCK_GUID = source.LOCK_GUID, " +
                            "dest.CREATE_DATE = source.CREATE_DATE, " +
                            "dest.TIMEOUT_DATE = source.TIMEOUT_DATE " +
                        "WHERE source.CREATE_DATE > dest.TIMEOUT_DATE";

    private static final String UNLOCK = "DELETE FROM " + TABLE_NAME + " WHERE LOCK_GUID = ?";

    DeliveryLockImpl(String name, int time, JdbcTemplate jdbcTemplate) {
        this.giud = UUID.randomUUID().toString();
        this.name = name;
        this.time = time;
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean tryLock() {
//        int affectedRows = jdbcTemplate.update(SET_LOCK, giud, name, time);
        int affectedRows = jdbcTemplate.update(SET_LOCK2);
        return affectedRows > 0;
    }

    public void unlock() {
        try {
            jdbcTemplate.update(UNLOCK, giud);
        } catch (Exception e) {
            log.error("При попытке отпустить lock({}) произошла ошибка: {}", name, e);
        }
    }

    @Override
    public String toString() {
        return "DeliveryLockImpl{"
                + "giud='" + giud + '\''
                + ", name='" + name + '\''
                + ", time=" + time
                + '}';
    }
}
