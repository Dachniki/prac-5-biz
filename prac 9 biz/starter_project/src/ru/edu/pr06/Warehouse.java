package ru.edu.pr06;

import java.util.*;

/**
 * Модуль складского комплекса (корневой агрегат учёта оборудования).
 * <p>
 * Архитектурные особенности:
 * <ul>
 *   <li>Хранилище позиций базируется на приватном {@link LinkedHashMap}, гарантируя O(1) поиск и
 *       детерминированный порядок регистрации;</li>
 *   <li>Журнал операций ведётся в {@link ArrayList} хронологически;</li>
 *   <li>Полная изоляция внутреннего состояния согласно правилу R6 (отсутствие прямых ссылок);</li>
 *   <li>Строгая валидация правил предметной области (R1–R9, Вариант 4).</li>
 * </ul>
 */
public class Warehouse {

    // Каталог номенклатуры с доступом O(1) и фиксацией порядка добавления
    private final Map<String, StockItem> inventory = new LinkedHashMap<>();

    // Хронологический реестр приходно-расходных ордеров
    private final List<StockMovement> auditLog = new ArrayList<>();

    /**
     * TODO 3: Регистрация новой номенклатурной позиции в каталоге склада.
     *
     * @param item карточка оборудования (R8: не null, R1: уникальный код)
     */
    public void addItem(StockItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Stock item cannot be null");
        }
        final String code = item.getCode();
        if (inventory.containsKey(code)) {
            throw new IllegalArgumentException("code already exists: " + code);
        }
        inventory.put(code, item);
    }

    /**
     * TODO 4: Оприходование поступившей партии оборудования.
     *
     * @param code артикул номенклатуры
     * @param q    объем поставки (q > 0)
     */
    public void receive(String code, int q) {
        StockItem target = findExistingItemOrThrow(code);
        target.increase(q);
        auditLog.add(new StockMovement(code, MovementType.RECEIVE, q));
    }

    /**
     * TODO 5: Отпуск (списание) оборудования со склада.
     * Регламентируется правилом R2 (по Варианту 4 остаток не опускается ниже 10 ед.).
     *
     * @param code артикул номенклатуры
     * @param q    количество к списанию (q > 0)
     */
    public void issue(String code, int q) {
        StockItem target = findExistingItemOrThrow(code);
        target.decrease(q);
        auditLog.add(new StockMovement(code, MovementType.ISSUE, q));
    }

    /**
     * TODO 6: Выборка номенклатурных позиций по категории.
     * Соблюдение R5: возвращается изолированная защитная копия списка.
     *
     * @param c целевая категория оборудования
     * @return список подходящих позиций
     */
    public List<StockItem> findByCategory(Category c) {
        if (c == null) {
            return new ArrayList<>();
        }
        List<StockItem> matched = new ArrayList<>();
        for (StockItem item : inventory.values()) {
            if (item.getCategory() == c) {
                matched.add(item);
            }
        }
        return matched;
    }

    /**
     * TODO 7: Сводная статистика складских остатков в разрезе категорий.
     *
     * @return ассоциативная карта категорий и суммарного количества единиц
     */
    public Map<Category, Integer> totalsByCategory() {
        Map<Category, Integer> distribution = new EnumMap<>(Category.class);
        for (StockItem item : inventory.values()) {
            distribution.merge(item.getCategory(), item.getQuantity(), Integer::sum);
        }
        return distribution;
    }

    /**
     * Вариант 4 (Часть Б): Экземплярный метод подсчёта суммарного запаса всех позиций.
     * Вычисляет аккумулированный остаток по всему активному каталогу.
     *
     * @return общее число единиц оборудования на складе
     */
    public int addTotalQuantity() {
        int cumulativeSum = 0;
        for (StockItem item : inventory.values()) {
            cumulativeSum += item.getQuantity();
        }
        return cumulativeSum;
    }

    /**
     * Вариант 4 (Часть Б): Статический фасад анализа суммарного остатка склада.
     *
     * @param wh экземпляр склада (R8: не null)
     * @return суммарное число единиц оборудования
     */
    public static int addTotalQuantity(Warehouse wh) {
        if (wh == null) {
            throw new IllegalArgumentException("Warehouse instance cannot be null");
        }
        return wh.addTotalQuantity();
    }

    /**
     * Вспомогательный метод поиска позиции с проверкой существования (R9, Fail-Fast).
     */
    private StockItem findExistingItemOrThrow(String code) {
        if (code == null) {
            throw new IllegalArgumentException("code is null");
        }
        StockItem item = inventory.get(code);
        if (item == null) {
            throw new IllegalArgumentException("code not found: " + code);
        }
        return item;
    }

    /**
     * Прямой доступ к карточке по коду (для чтения).
     */
    public StockItem getItem(String code) {
        return inventory.get(code);
    }

    /**
     * Общее число уникальных товарных позиций на складе (R6).
     */
    public int itemsCount() {
        return inventory.size();
    }

    /**
     * Общее количество зарегистрированных операций в журнале (R6).
     */
    public int movementsCount() {
        return auditLog.size();
    }

    /**
     * Получение защитной копии списка всех номенклатурных позиций (R6).
     */
    public List<StockItem> getAllItems() {
        return new ArrayList<>(inventory.values());
    }

    /**
     * Получение защитной копии реестра перемещений (R6).
     */
    public List<StockMovement> getAllMovements() {
        return new ArrayList<>(auditLog);
    }
}
