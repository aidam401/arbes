package com.phonecompany.billing.service;


import com.phonecompany.billing.model.Log;
import com.phonecompany.billing.model.Zone;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final String DATA_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final LocalTime CHEAPER_ZONE_END = LocalTime.of(8, 0, 0, 0);
    private static final LocalTime CHEAPER_ZONE_START = LocalTime.of(16, 0, 0, 0);
    private static final BigDecimal NORMAL_PRICE = BigDecimal.ONE;
    private static final BigDecimal CHEAPER_PRICE = BigDecimal.valueOf(0.5);
    private static final BigDecimal NORMAL_PRICE_AFTER_SOME_MIN = BigDecimal.valueOf(0.8);
    private static final BigDecimal CHEAPER_PRICE_AFTER_SOME_MIN = BigDecimal.valueOf(0.3);
    private static final Integer NON_DISCOUNTED_MINUTES = 5;

    @Override
    public BigDecimal calculate(String phoneLog) {
        if (phoneLog == null || phoneLog.equals("")) {
            return BigDecimal.ZERO;
        }
        List<Log> logs = parseTextLog(phoneLog);
        deleteWrongLogs(logs);
        currentZone(LocalDateTime.now().plusHours(10));

        String freeNumber = getFreeNumber(logs);
        return calculatePrice(logs, freeNumber);
    }

    private String getFreeNumber(List<Log> logs) {
        var occurMap = new HashMap<String, Long>();
        long maxOccurrence = 0L;

        //Get map and find max
        for (Log log : logs) {
            String number = log.getNumber();
            long numOfOccurrence = occurMap.getOrDefault(number, 0L) + 1;
            if (maxOccurrence < numOfOccurrence) {
                maxOccurrence = numOfOccurrence;
            }
            occurMap.put(number, numOfOccurrence);
        }

        //Remove smaller
        var iterator = occurMap.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue() < maxOccurrence) {
                iterator.remove();
            }
        }
        //Calculate sums
        if (occurMap.size() != 1) {
            var maxVal = 0;
            var maxNum = "";
            iterator = occurMap.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                int sum = sumOfDigits(entry.getKey());
                if (sum > maxVal) {
                    maxVal = sum;
                    maxNum = entry.getKey();
                }
            }
            return maxNum;
        }
        return occurMap.keySet().stream().toList().get(0);
    }

    private static int sumOfDigits(String number) {
        //Only one spot where I am not avoiding streams, bc I am too lazy.
        return number.chars()
                .map(Character::getNumericValue)
                .sum();
    }

    private void deleteWrongLogs(List<Log> logs) {
        logs.removeIf(log -> log.getStartTime().isAfter(log.getEndTime()));
    }

    private BigDecimal calculatePrice(List<Log> logs, String freeNumber) {
        var price = BigDecimal.ZERO;
        for (Log log : logs) {
            if (!log.getNumber().equals(freeNumber)) {
                price = price.add(calculatePricePerCall(log));
            }
        }
        return price;
    }

    private BigDecimal calculatePricePerCall(Log log) {
        BigDecimal currentPrice = BigDecimal.ZERO;


        LocalDateTime endTime = log.getEndTime();
        int nonDiscountedMinutes = NON_DISCOUNTED_MINUTES;

        LocalDateTime currentTime = log.getStartTime();
        Zone currentZone = currentZone(currentTime);

        //Calculating price in NON_DISCOUNTED_MINUTES
        while (currentTime.isBefore(endTime) && nonDiscountedMinutes > 0) {
            if (currentZone == Zone.NORMAL) {
                currentPrice = currentPrice.add(NORMAL_PRICE);
            } else {
                currentPrice = currentPrice.add(CHEAPER_PRICE);
            }
            currentTime = currentTime.plusMinutes(1);
            nonDiscountedMinutes--;
            currentZone = currentZone(currentTime);
        }

        //Calculating price after
        while (currentTime.isBefore(endTime)) {
            BigDecimal minutesUntilZoneChange = minutesUntilZoneChangeOrEnd(currentTime, endTime);
            if (currentZone == Zone.NORMAL) {
                currentPrice = currentPrice.add(NORMAL_PRICE_AFTER_SOME_MIN.multiply(minutesUntilZoneChange));
            } else {
                currentPrice = currentPrice.add(CHEAPER_PRICE_AFTER_SOME_MIN.multiply(minutesUntilZoneChange));
            }
            currentTime = currentTime.plusMinutes(minutesUntilZoneChange.longValue()+1);
            currentZone = currentZone(currentTime);
        }
        return currentPrice;

    }

    private Zone nextMinuteZone(LocalDateTime dateTime) {
        Zone currZone = currentZone(dateTime);
        if (currZone == currentZone(dateTime.plusMinutes(1))) {
            return currZone;
        } else {
            return currZone == Zone.NORMAL ? Zone.CHEAPER : Zone.NORMAL;
        }
    }

    private Zone currentZone(LocalDateTime dateTime) {
        var time = dateTime.toLocalTime();
        return !((CHEAPER_ZONE_END.isBefore(time) || CHEAPER_ZONE_END.equals(time)) && (CHEAPER_ZONE_START.isAfter(time) || CHEAPER_ZONE_START.equals(time))) ? Zone.CHEAPER : Zone.NORMAL;
    }

    private BigDecimal minutesUntilZoneChangeOrEnd(LocalDateTime dateTime, LocalDateTime endTime) {
        if (timeToNextZone(dateTime).isAfter(endTime))
            return new BigDecimal(ChronoUnit.MINUTES.between(dateTime, endTime));
        return minutesUntilZoneChange(dateTime);

    }

    private BigDecimal minutesUntilZoneChange(LocalDateTime dateTime) {
        if (currentZone(dateTime) == Zone.CHEAPER)
            return new BigDecimal(ChronoUnit.MINUTES.between(dateTime, nextNormalZone(dateTime)));
        return new BigDecimal(ChronoUnit.MINUTES.between(dateTime, nextCheaperZone(dateTime)));
    }

    private LocalDateTime timeToNextZone(LocalDateTime dateTime) {
        if (currentZone(dateTime) == Zone.CHEAPER)
            return nextNormalZone(dateTime);
        return nextCheaperZone(dateTime);
    }

    private LocalDateTime nextCheaperZone(LocalDateTime dateTime) {
        var nextZone = dateTime.withHour(CHEAPER_ZONE_START.getHour()).withMinute(CHEAPER_ZONE_START.getMinute()).withSecond(CHEAPER_ZONE_START.getSecond()).withNano(CHEAPER_ZONE_START.getNano());
        if (dateTime.isAfter(nextZone)) {
            return nextZone.plusDays(1);
        }
        return nextZone;
    }

    private LocalDateTime nextNormalZone(LocalDateTime dateTime) {
        var nextZone = dateTime.withHour(CHEAPER_ZONE_END.getHour()).withMinute(CHEAPER_ZONE_END.getMinute()).withSecond(CHEAPER_ZONE_END.getSecond()).withNano(CHEAPER_ZONE_END.getNano());
        if (dateTime.isAfter(nextZone)) {
            return nextZone.plusDays(1);
        }
        return nextZone;
    }

    private List<Log> parseTextLog(String phoneLog) {
        var formatter = DateTimeFormatter.ofPattern(DATA_FORMAT);
        var list = new ArrayList<Log>();
        for (String strLog : phoneLog.split("\\r?\\n")) {
            String[] args = strLog.split(",");
            list.add(new Log(args[0], LocalDateTime.parse(args[1], formatter), LocalDateTime.parse(args[2], formatter)));
        }
        return list;
    }


}
