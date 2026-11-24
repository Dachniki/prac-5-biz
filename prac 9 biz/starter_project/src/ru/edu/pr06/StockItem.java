package ru.edu.pr06;

import java.util.Objects;

/**
 * Учётная карточка складской единицы (номенклатурная позиция).
 * <p>
 * Обеспечивает соблюдение инвариантов бизнес-логики:
 * - Уникальный код и наименование не могут быть null или пустыми (R8);
 * - Наличие категории обязательно (R4);
 * - Начальный складской остаток не может быть отрицательным (R7);
 * - Списание регламентируется правилом R2 (по Варианту 4: неснижаемый запас >= 10 шт.).
 */
public class StockItem {

    /** Минимально допустимый резерв остатка согласно Варианту 4 (Часть А) */
    public static final int MIN_RESERVE_THRESHOLD = 10;

    private final String code;
    private final String name;
    private final Category category;
    private int quantity;

    /**
     * Конструктор номенклатурной позиции с валидацией контракта (R7, R8, R4).
     *
     * @param code     инвентарный код позиции
     * @param name     наименование оборудования
     * @param category категория оборудования
     * @param quantity начальный остаток
     */
    public StockItem(String code, String name, Category category, int quantity) {
        validateAttributes(code, name, category, quantity);

        this.code = code.trim();
        this.name = name.trim();
        this.category = category;
        this.quantity = quantity;
    }

    private static void validateAttributes(String code, String name, Category category, int quantity) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("code is null or blank");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is null or blank");
        }
        if (category == null) {
            throw new IllegalArgumentException("category is null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be >= 0");
        }
    }

    /**
     * TODO 1: Увеличение остатка позиции при поступлении.
     *
     * @param q количество поступивших единиц (должно быть > 0)
     */
    public void increase(int q) {
        if (q <= 0) {
            throw new IllegalArgumentException("q must be > 0");
        }
        this.quantity += q;
    }

    /**
     * TODO 2 / Вариант 4 (Часть А): Списание со склада с контролем неснижаемого резерва (R2).
     * <p>
     * Списание блокируется, если передано неположительное количество (q <= 0)
     * либо результирующий остаток опускается ниже нормативного запаса (10 единиц).
     *
     * @param q запрашиваемое к списанию количество
     */
    public void decrease(int q) {
        if (q <= 0) {
            throw new IllegalArgumentException("q must be > 0");
        }
        final int projectedBalance = this.quantity - q;
        if (projectedBalance < MIN_RESERVE_THRESHOLD) {
            throw new IllegalArgumentException(String.format(
                    "Operation violates minimum reserve rule (R2, variant 4): current=%d, requested=%d, min_required=%d",
                    this.quantity, q, MIN_RESERVE_THRESHOLD
            ));
        }
        this.quantity = projectedBalance;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StockItem other)) return false;
        return Objects.equals(this.code, other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return String.format("StockItem[code='%s', name='%s', cat=%s, qty=%d]",
                code, name, category, quantity);
    }
}
