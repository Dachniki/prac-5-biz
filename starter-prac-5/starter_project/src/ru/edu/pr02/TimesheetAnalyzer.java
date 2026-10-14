package ru.edu.pr02;

public class TimesheetAnalyzer {

    private static final int DAYS_PER_WEEK = 5;
    private static final int DAILY_NORMAL_HOURS = 8;
    private static final int WEEKLY_NORMAL_HOURS = 40;
    private static final int MAX_DAILY_HOURS = 16;
    private static final int MIN_DAILY_HOURS = 0;

    public static int[] parseHours(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Ошибка: структура - строка не может быть null");
        }
        String[] parts = line.split(";", -1);
        if (parts.length != DAYS_PER_WEEK) {
            throw new IllegalArgumentException(
                "Ошибка: структура - ожидалось " + DAYS_PER_WEEK +
                " значений, получено " + parts.length);
        }
        int[] result = new int[DAYS_PER_WEEK];
        for (int i = 0; i < DAYS_PER_WEEK; i++) {
            String part = parts[i];
            if (!part.matches("^(0|[1-9][0-9]*)$")) {
                throw new IllegalArgumentException(
                    "Ошибка: позиция " + (i + 1) +
                    " - значение \"" + part + "\" не является целым числом");
            }
            int v = Integer.parseInt(part);
            if (v < MIN_DAILY_HOURS || v > MAX_DAILY_HOURS) {
                throw new IllegalArgumentException(
                    "Ошибка: позиция " + (i + 1) +
                    " - значение " + v + " вне диапазона [0, 16]");
            }
            result[i] = v;
        }
        return result;
    }

    public static int total(int[] hours) {
        validateArray(hours);
        int sum = 0;
        for (int v : hours) {
            sum += v;
        }
        return sum;
    }

    public static double average(int[] hours) {
        validateArray(hours);
        double avg = (double) total(hours) / DAYS_PER_WEEK;
        return Math.round(avg * 100.0) / 100.0;
    }

    public static int overtimeDays(int[] hours) {
        validateArray(hours);
        int count = 0;
        for (int v : hours) {
            if (v > DAILY_NORMAL_HOURS) {
                count++;
            }
        }
        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        validateArray(hours);
        return Math.max(0, WEEKLY_NORMAL_HOURS - total(hours));
    }

    public static int addRestHours(int[] hours) {
        validateArray(hours);
        int rest = DAYS_PER_WEEK * DAILY_NORMAL_HOURS - total(hours);
        return Math.max(0, rest);
    }

    private static void validateArray(int[] hours) {
        if (hours == null) {
            throw new IllegalArgumentException("hours is null");
        }
        if (hours.length != DAYS_PER_WEEK) {
            throw new IllegalArgumentException(
                "hours length must be " + DAYS_PER_WEEK +
                ", got " + hours.length);
        }
    }
}
