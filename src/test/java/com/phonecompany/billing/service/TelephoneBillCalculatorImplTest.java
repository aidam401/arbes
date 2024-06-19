package com.phonecompany.billing.service;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class TelephoneBillCalculatorImplTest {

    private TelephoneBillCalculatorImpl calculator;

    @Before
    public void setUp() {
        calculator = new TelephoneBillCalculatorImpl();
    }

    @Test
    public void testCalculate_emptyLog() {
        String phoneLog = "";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void testCalculate_nullLog() {
        BigDecimal result = calculator.calculate(null);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void testCalculate_singleLog() {
        String phoneLog = "000000000000,01-01-2023 07:00:00,01-01-2023 07:05:00";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    public void testCalculate_multipleLogs_cheaper_zone_expensive_minutes() {
        String phoneLog = "000000000000,01-01-2023 07:00:00,01-01-2023 07:05:00\n" +
                "999999999999,01-01-2023 17:00:00,01-01-2023 17:05:00";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("2.5"), result); // 5 minutes in normal zone + 10 minutes in cheaper zone
    }
    @Test
    public void testCalculate_multipleLogs_discounted_zone_half_minutes() {
        String phoneLog = "000000000000,01-01-2023 07:00:00,01-01-2023 07:10:00\n" +
                "999999999999,01-01-2023 17:00:00,01-01-2023 17:05:00";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("4.0"), result); // 5 minutes in normal zone + 10 minutes in cheaper zone
    }
    @Test
    public void testCalculate_multipleLogs_normal_zone__expensive_minutes() {
        String phoneLog = "000000000000,01-01-2023 08:00:00,01-01-2023 08:05:00\n" +
                "999999999999,01-01-2023 17:00:00,01-01-2023 17:05:00";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("5"), result); // 5 minutes in normal zone + 10 minutes in cheaper zone
    }
    @Test
    public void testCalculate_multipleLogs_normal_zone_half_minutes() {
        String phoneLog = "000000000000,01-01-2023 08:00:00,01-01-2023 08:10:00\n" +
                "999999999999,01-01-2023 17:00:00,01-01-2023 17:05:00";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("9.0"), result); // 5 minutes in normal zone + 10 minutes in cheaper zone
    }
    @Test
    public void testCalculate_multipleLogs_accross_zones() {
        String phoneLog = """
                000000000000,01-01-2023 08:00:00,02-01-2023 08:00:00
                000000000001,01-01-2023 08:00:00,01-01-2023 08:10:00
                999999999999,01-01-2023 17:00:00,01-01-2023 17:05:00""";
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("681.7"), result); // 5 minutes in normal zone + 10 minutes in cheaper zone
    }


}
