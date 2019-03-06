package com.github.senin24.dbtests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DeliveryLockImplTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void tryLock() {
        int countRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "DELIVERY_LOCK");
        Assert.assertEquals(countRows, 0);
        DeliveryLockImpl deliveryLock = new DeliveryLockImpl("11", 10, jdbcTemplate);
        boolean isLock = deliveryLock.tryLock();
        Assert.assertTrue(isLock);
        countRows = JdbcTestUtils.countRowsInTable(jdbcTemplate, "DELIVERY_LOCK");
        Assert.assertEquals(countRows, 1);
    }

    @Test
    public void unlock() {
    }
}