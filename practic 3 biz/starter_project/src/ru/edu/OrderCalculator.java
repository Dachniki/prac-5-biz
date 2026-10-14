package ru.edu.pr01;

public class OrderCalculator {

    // ===== Границы по базовому заданию =====
    public static final int    MAX_QUANTITY = 10_000;
    public static final double MAX_PRICE    = 5_000_000.0;
    public static final double MAX_DISCOUNT = 30.0;

    // ===== ВАРИАНТ 6: порог бесплатной обработки =====
    // Если базовая сумма < 35 → добавляется PROCESSING_FEE, иначе — 0.
    public static final double FREE_PROCESSING_THRESHOLD = 35.0;
    public static final double PROCESSING_FEE            = 100.0; // уточнить у преподавателя

    // ---------- TODO 1 ----------
    public static boolean isValid(int quantity, double unitPrice, double discountPercent) {
        if (quantity <= 0 || quantity > MAX_QUANTITY) return false;
        if (unitPrice <= 0 || unitPrice > MAX_PRICE) return false;
        if (discountPercent < 0 || discountPercent > MAX_DISCOUNT) return false;
        return true;
    }

    // ---------- TODO 2 ----------
    public static double calculateBase(int quantity, double unitPrice) {
        return quantity * unitPrice;
    }

    // ---------- TODO 3 ----------
    public static double applyDiscount(double base, double discountPercent) {
        return base * (1 - discountPercent / 100.0);
    }

    // ---------- TODO 4 ----------
    // По логике цепочки возвращает сумму С НДС (не сам налог).
    public static double calculateVat(double discounted, double vatPercent) {
        return discounted * (1 + vatPercent / 100.0);
    }

    // ---------- TODO 5 ----------
    public static double calculateTotal(int quantity, double unitPrice,
                                        double discountPercent, double vatPercent) {
        if (!isValid(quantity, unitPrice, discountPercent)) {
            throw new IllegalArgumentException("Некорректные входные данные");
        }
        double base       = calculateBase(quantity, unitPrice);
        double discounted = applyDiscount(base, discountPercent);
        double withVat    = calculateVat(discounted, vatPercent);

        // ВАРИАНТ 6: порог бесплатной обработки
        double fee = (base < FREE_PROCESSING_THRESHOLD) ? PROCESSING_FEE : 0.0;

        return withVat + fee;
    }
}