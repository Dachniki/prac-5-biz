package ru.edu.pr06;

import java.util.Objects;

/**
 * Запись журнала складских операций (хроника перемещений).
 *
 * @param itemCode артикул/код оборудования
 * @param type     тип операции (приход или расход)
 * @param quantity объем операции
 */
public record StockMovement(String itemCode, MovementType type, int quantity) {

    public StockMovement {
        Objects.requireNonNull(itemCode, "itemCode cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        if (itemCode.isBlank()) {
            throw new IllegalArgumentException("itemCode cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be strictly positive");
        }
    }
}
