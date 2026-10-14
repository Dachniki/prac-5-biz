package ru.edu.pr02;

import java.nio.file.*;
import java.util.*;

public class SelfCheck {

    static int p, f;

    public static void main(String[] a) {
        u(); d(); i();
        System.out.println("\nИтого: " + p + " pass, " + f + " fail");
    }

    static void t(String n, Runnable r) {
        try { r.run(); System.out.println("PASS " + n); p++; }
        catch (Throwable e) { System.out.println("FAIL " + n + ": " + e.getMessage()); f++; }
    }

    static void eq(int e, int v) { if (e != v) throw new RuntimeException(e + "!=" + v); }
    static void eq(double e, double v) { if (Math.abs(e - v) > 0.001) throw new RuntimeException(e + "!=" + v); }
    static void eq(int[] e, int[] v) {
        if (e.length != v.length) throw new RuntimeException("длина");
        for (int i = 0; i < e.length; i++) if (e[i] != v[i]) throw new RuntimeException("i=" + i);
    }
    static void err(Runnable r, String s) {
        try { r.run(); } catch (IllegalArgumentException e) {
            if (e.getMessage() == null || !e.getMessage().contains(s)) throw new RuntimeException(e.getMessage());
            return;
        }
        throw new RuntimeException("нет исключения");
    }

    static void u() {
        System.out.println("=== Unit ===");

        t("parseHours: корректная строка", () ->
            eq(new int[]{8,9,10,8,8}, TimesheetAnalyzer.parseHours("8;9;10;8;8")));
        t("parseHours: пустая строка", () ->
            err(() -> TimesheetAnalyzer.parseHours(""), "структура"));
        t("parseHours: значение 17 вне диапазона", () ->
            err(() -> TimesheetAnalyzer.parseHours("8;8;17;8;8"), "позиция 3"));
        t("parseHours: не число abc", () ->
            err(() -> TimesheetAnalyzer.parseHours("8;abc;8;8;8"), "позиция 2"));
        t("parseHours: 4 значения", () ->
            err(() -> TimesheetAnalyzer.parseHours("8;8;8;8"), "структура"));
        t("parseHours: trailing ;", () ->
            err(() -> TimesheetAnalyzer.parseHours("8;8;8;8;8;"), "структура"));
        t("parseHours: ведущий ноль 08", () ->
            err(() -> TimesheetAnalyzer.parseHours("08;8;8;8;8"), "позиция 1"));

        t("total: типовое 43", () ->
            eq(43, TimesheetAnalyzer.total(new int[]{8,9,10,8,8})));
        t("total: нижняя граница 0", () ->
            eq(0, TimesheetAnalyzer.total(new int[]{0,0,0,0,0})));
        t("total: верхняя граница 80", () ->
            eq(80, TimesheetAnalyzer.total(new int[]{16,16,16,16,16})));

        t("average: типовое 8.6", () ->
            eq(8.6, TimesheetAnalyzer.average(new int[]{8,9,10,8,8})));
        t("average: целое 8.0", () ->
            eq(8.0, TimesheetAnalyzer.average(new int[]{8,8,8,8,8})));
        t("average: дробное 7.8", () ->
            eq(7.8, TimesheetAnalyzer.average(new int[]{7,8,9,8,7})));

        t("overtimeDays: нет переработки", () ->
            eq(0, TimesheetAnalyzer.overtimeDays(new int[]{8,8,8,8,8})));
        t("overtimeDays: два дня", () ->
            eq(2, TimesheetAnalyzer.overtimeDays(new int[]{9,8,7,10,8})));
        t("overtimeDays: граница 8", () ->
            eq(1, TimesheetAnalyzer.overtimeDays(new int[]{8,9,8,8,8})));

        t("weeklyOvertime: сумма 40 -> 0", () ->
            eq(0, TimesheetAnalyzer.weeklyOvertime(new int[]{8,8,8,8,8})));
        t("weeklyOvertime: сумма 50 -> 0", () ->
            eq(0, TimesheetAnalyzer.weeklyOvertime(new int[]{10,10,10,10,10})));
        t("weeklyOvertime: сумма 30 -> 10", () ->
            eq(10, TimesheetAnalyzer.weeklyOvertime(new int[]{6,6,6,6,6})));
        t("weeklyOvertime (A): сумма 45 -> 0", () ->
            eq(0, TimesheetAnalyzer.weeklyOvertime(new int[]{9,9,9,9,9})));
        t("weeklyOvertime (A): сумма 35 -> 5", () ->
            eq(5, TimesheetAnalyzer.weeklyOvertime(new int[]{7,7,7,7,7})));

        t("addRestHours: сумма 40 -> 0", () ->
            eq(0, TimesheetAnalyzer.addRestHours(new int[]{8,8,8,8,8})));
        t("addRestHours: сумма 25 -> 15", () ->
            eq(15, TimesheetAnalyzer.addRestHours(new int[]{5,5,5,5,5})));

        t("total: null -> IAE", () ->
            err(() -> TimesheetAnalyzer.total(null), "null"));
        t("average: длина != 5 -> IAE", () ->
            err(() -> TimesheetAnalyzer.average(new int[]{8,8}), "length"));
    }

    static void d() {
        System.out.println("\n=== Data ===");
        List<String> ls;
        try { ls = Files.readAllLines(Paths.get("data/sample.csv")); }
        catch (Exception e) { System.out.println("FAIL: " + e.getMessage()); return; }
        int n = 0, k = 0;
        for (int i = 0; i < ls.size(); i++) {
            String s = ls.get(i).trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            String[] q = s.split("\\|", -1);
            if (q.length < 3) { n++; continue; }
            String in = q[0].equals("EMPTY") ? "" : q[0], ex = q[1].trim();
            n++;
            try {
                if (ex.startsWith("OK:")) {
                    int[] h = TimesheetAnalyzer.parseHours(in);
                    eq(gi(ex,"sum="), TimesheetAnalyzer.total(h));
                    eq(gd(ex,"avg="), TimesheetAnalyzer.average(h));
                    eq(gi(ex,"od="),  TimesheetAnalyzer.overtimeDays(h));
                    eq(gi(ex,"wo="),  TimesheetAnalyzer.weeklyOvertime(h));
                    System.out.println("PASS data/" + (i+1)); k++;
                } else if (ex.startsWith("ERROR:")) {
                    err(() -> TimesheetAnalyzer.parseHours(in), ex.substring(6).trim());
                    System.out.println("PASS data/" + (i+1)); k++;
                }
            } catch (Throwable e) { System.out.println("FAIL data/" + (i+1) + ": " + e.getMessage()); }
        }
        System.out.println("Data: " + k + "/" + n);
    }

    static int gi(String s, String k) {
        int i = s.indexOf(k) + k.length(), j = i;
        while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '-')) j++;
        return Integer.parseInt(s.substring(i, j));
    }

    static double gd(String s, String k) {
        int i = s.indexOf(k) + k.length(), j = i;
        while (j < s.length() && (Character.isDigit(s.charAt(j)) || s.charAt(j) == '.' || s.charAt(j) == '-')) j++;
        return Double.parseDouble(s.substring(i, j));
    }

    static void i() {
        System.out.println("\n=== Ввод (exit) ===");
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String l = sc.nextLine();
            if (l.equals("exit") || l.isEmpty()) break;
            try {
                int[] h = TimesheetAnalyzer.parseHours(l);
                System.out.println("Сумма: " + TimesheetAnalyzer.total(h));
                System.out.println("Среднее: " + TimesheetAnalyzer.average(h));
                System.out.println("Дней переработки: " + TimesheetAnalyzer.overtimeDays(h));
                System.out.println("Недельная переработка: " + TimesheetAnalyzer.weeklyOvertime(h));
                System.out.println("Отдых: " + TimesheetAnalyzer.addRestHours(h));
            } catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
        }
    }
}
