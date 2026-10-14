package ru.edu.pr01;

public class SelfCheck {

    static int ok = 0, fail = 0;

    static void check(String name, boolean cond) {
        System.out.printf("[%s] %s%n", cond ? "OK" : "FAIL", name);
        if (cond) ok++; else fail++;
    }

    static void near(String name, double a, double e) {
        check(name + " (got " + a + ", expected " + e + ")", Math.abs(a - e) < 0.001);
    }

    static void throwsIAE(String name, Runnable r) {
        try { r.run(); check(name, false); }
        catch (IllegalArgumentException ex) { check(name, true); }
        catch (Exception ex) { check(name, false); }
    }

    public static void main(String[] args) {
        System.out.println("=== SelfCheck (вариант 6) ===\n");

        // 1. Типовое значение
        near("calculateBase(2,100)",
             OrderCalculator.calculateBase(2, 100.0), 200.0);
        near("calculateTotal(2,100,10,20)",
             OrderCalculator.calculateTotal(2, 100.0, 10.0, 20.0), 216.0);

        // 2. Границы quantity
        check("quantity=1 валидно",        OrderCalculator.isValid(1, 100.0, 0.0));
        check("quantity=0 невалидно",     !OrderCalculator.isValid(0, 100.0, 0.0));
        check("quantity=10000 валидно",    OrderCalculator.isValid(10_000, 100.0, 0.0));
        check("quantity=10001 невалидно", !OrderCalculator.isValid(10_001, 100.0, 0.0));

        // 3. Границы price и discount
        check("price=5 000 000 валидно",    OrderCalculator.isValid(1, 5_000_000.0, 0.0));
        check("price=5 000 001 невалидно", !OrderCalculator.isValid(1, 5_000_001.0, 0.0));
        check("discount=30 валидно",        OrderCalculator.isValid(1, 100.0, 30.0));
        check("discount=31 невалидно",     !OrderCalculator.isValid(1, 100.0, 31.0));

        // 4. Вариант 6: порог бесплатной обработки = 35
        near("base=30 (<35) → +PROCESSING_FEE",
             OrderCalculator.calculateTotal(1, 30.0, 0.0, 0.0),
             30.0 + OrderCalculator.PROCESSING_FEE);
        near("base=100 (≥35) → без платы",
             OrderCalculator.calculateTotal(1, 100.0, 0.0, 0.0), 100.0);

        // 5. Порядок операций: НДС после скидки
        // base=1000, -10% → 900, +20% → 1080
        near("1000 → -10% → +20% = 1080",
             OrderCalculator.calculateTotal(10, 100.0, 10.0, 20.0), 1080.0);

        // 6. Исключения при невалидных данных
        throwsIAE("quantity=0 → IAE",
                  () -> OrderCalculator.calculateTotal(0, 100.0, 0.0, 20.0));
        throwsIAE("price=0 → IAE",
                  () -> OrderCalculator.calculateTotal(1, 0.0, 0.0, 20.0));
        throwsIAE("discount=31 → IAE",
                  () -> OrderCalculator.calculateTotal(1, 100.0, 31.0, 20.0));

        // Итог
        System.out.printf("%nПройдено: %d, провалено: %d → %s%n",
                          ok, fail, fail == 0 ? "PASS" : "FAIL");
    }
}