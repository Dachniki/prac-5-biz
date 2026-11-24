package ru.edu.pr06;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Верификационный комплекс и тестовый стенд для Практической работы №9.
 * <p>
 * Студент: Щеглов Никита Андреевич
 * Группа: ЭФБО-12-25
 * Вариант: 4
 */
public class SelfCheck {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        if (args.length > 0 && ("-i".equalsIgnoreCase(args[0]) || "--interactive".equalsIgnoreCase(args[0]))) {
            startInteractiveConsole();
            return;
        }

        printBanner();
        executeUnitVerificationSuite();
        executeDataDrivenTestPipeline();
        printSummaryReport();
    }

    private static void printBanner() {
        System.out.println("================================================================");
        System.out.println("   ПРАКТИЧЕСКАЯ РАБОТА №9. КОНТРОЛЬНО-ПРОВЕРОЧНЫЙ СТЕНД");
        System.out.println("   Студент: Щеглов Никита Андреевич | Группа: ЭФБО-12-25");
        System.out.println("   Индивидуальный вариант №4");
        System.out.println("================================================================\n");
    }

    private static void executeUnitVerificationSuite() {
        System.out.println(">>> [ЭТАП 1] Запуск модульных проверок спецификации (R1-R9 + Вариант 4)");

        // 1. Конструктор StockItem
        assertCondition("01. Корректная инициализация карточки StockItem", () -> {
            StockItem item = new StockItem("IT-101", "Рабочая станция", Category.COMPUTER, 15);
            return "IT-101".equals(item.getCode()) &&
                   "Рабочая станция".equals(item.getName()) &&
                   item.getCategory() == Category.COMPUTER &&
                   item.getQuantity() == 15;
        });

        // 2. Инвариант R7: quantity < 0
        assertThrows("02. Правило R7: Инициализация StockItem с отрицательным остатком",
                IllegalArgumentException.class,
                () -> new StockItem("IT-102", "Некондиция", Category.COMPUTER, -5));

        // 3. Инвариант R8: пустой код позиции
        assertThrows("03. Правило R8: Пустой или пробельный инвентарный код",
                IllegalArgumentException.class,
                () -> new StockItem("   ", "Монитор", Category.DISPLAY, 10));

        // 4. Инвариант R8: пустое наименование
        assertThrows("04. Правило R8: Null в качестве наименования позиции",
                IllegalArgumentException.class,
                () -> new StockItem("IT-103", null, Category.COMPUTER, 10));

        // 5. Инвариант R4: отсутствие категории
        assertThrows("05. Правило R4: Нулевая ссылка на категорию",
                IllegalArgumentException.class,
                () -> new StockItem("IT-104", "Ноутбук", null, 10));

        // 6. TODO 1: Увеличение остатка (increase)
        assertCondition("06. TODO 1: Корректное пополнение остатка (15 + 10 = 25)", () -> {
            StockItem item = new StockItem("IT-105", "Маршрутизатор", Category.NETWORK, 15);
            item.increase(10);
            return item.getQuantity() == 25;
        });

        // 7. TODO 1: Пополнение на неположительное значение
        assertThrows("07. TODO 1: Пополнение с q <= 0 вызывает исключение",
                IllegalArgumentException.class,
                () -> {
                    StockItem item = new StockItem("IT-106", "Свитч", Category.NETWORK, 15);
                    item.increase(0);
                });

        // 8. TODO 2 & Вариант 4: Списание в пределах резерва (15 - 5 = 10, остаток ровно 10)
        assertCondition("08. Вариант 4 (R2): Списание ровно до порога резерва 10 (15 - 5 = 10)", () -> {
            StockItem item = new StockItem("IT-107", "Клавиатура", Category.PERIPHERAL, 15);
            item.decrease(5);
            return item.getQuantity() == 10;
        });

        // 9. TODO 2 & Вариант 4: Нарушение границы резерва (15 - 6 = 9 < 10)
        assertThrows("09. Вариант 4 (R2): Запрет списания ниже резерва 10 шт. (15 - 6 = 9)",
                IllegalArgumentException.class,
                () -> {
                    StockItem item = new StockItem("IT-108", "Мышь", Category.PERIPHERAL, 15);
                    item.decrease(6);
                });

        // 10. TODO 2: Списание неположительного объема
        assertThrows("10. TODO 2: Попытка списания q <= 0",
                IllegalArgumentException.class,
                () -> {
                    StockItem item = new StockItem("IT-109", "Монитор", Category.DISPLAY, 20);
                    item.decrease(-3);
                });

        // 11. TODO 3: Добавление позиции на склад
        assertCondition("11. TODO 3: Успешное добавление карточки в агрегат Warehouse", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("SRV-1", "Сервер баз данных", Category.COMPUTER, 20));
            return wh.itemsCount() == 1 && wh.getItem("SRV-1") != null;
        });

        // 12. Инвариант R1: Запрет коллизии артикулов
        assertThrows("12. Правило R1: Блокировка добавления дубликата инвентарного кода",
                IllegalArgumentException.class,
                () -> {
                    Warehouse wh = new Warehouse();
                    wh.addItem(new StockItem("SRV-1", "Сервер 1", Category.COMPUTER, 20));
                    wh.addItem(new StockItem("SRV-1", "Сервер Клон", Category.COMPUTER, 15));
                });

        // 13. TODO 4: Операция прихода (receive) и фиксация в аудит-логе (R3)
        assertCondition("13. TODO 4 & R3: Приход партии с регистрацией движения в аудит-логе", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("KB-1", "Клавиатура", Category.PERIPHERAL, 15));
            wh.receive("KB-1", 10);
            return wh.getItem("KB-1").getQuantity() == 25 &&
                   wh.movementsCount() == 1 &&
                   wh.getAllMovements().get(0).type() == MovementType.RECEIVE;
        });

        // 14. Инвариант R9: Приход на несуществующую позицию
        assertThrows("14. Правило R9: Оприходование по отсутствующему коду",
                IllegalArgumentException.class,
                () -> {
                    Warehouse wh = new Warehouse();
                    wh.receive("UNKNOWN-CODE", 5);
                });

        // 15. TODO 5 & Вариант 4: Успешное списание со склада с сохранением резерва
        assertCondition("15. TODO 5 & Вариант 4: Списание 8 единиц при начальных 20 (остаток 12 >= 10)", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("DS-1", "Дисплей 4K", Category.DISPLAY, 20));
            wh.issue("DS-1", 8);
            return wh.getItem("DS-1").getQuantity() == 12 &&
                   wh.movementsCount() == 1 &&
                   wh.getAllMovements().get(0).type() == MovementType.ISSUE;
        });

        // 16. TODO 5 & Вариант 4: Попытка списания со склада ниже порога 10 ед.
        assertThrows("16. Вариант 4: Отклонение выдачи со склада, если остаток упадет ниже 10",
                IllegalArgumentException.class,
                () -> {
                    Warehouse wh = new Warehouse();
                    wh.addItem(new StockItem("DS-2", "Дисплей FHD", Category.DISPLAY, 20));
                    wh.issue("DS-2", 11); // 20 - 11 = 9 < 10 -> отказ
                });

        // 17. Инвариант R9: Выдача несуществующей позиции
        assertThrows("17. Правило R9: Попытка выдачи отсутствующего кода",
                IllegalArgumentException.class,
                () -> {
                    Warehouse wh = new Warehouse();
                    wh.issue("NON_EXISTENT", 2);
                });

        // 18. TODO 6 & R5: Фильтрация по категории и защита от модификации оригинала
        assertCondition("18. TODO 6 & R5: findByCategory возвращает независимую защитную копию", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("C-1", "ПК 1", Category.COMPUTER, 15));
            wh.addItem(new StockItem("C-2", "ПК 2", Category.COMPUTER, 20));
            wh.addItem(new StockItem("D-1", "Монитор", Category.DISPLAY, 12));

            List<StockItem> computers = wh.findByCategory(Category.COMPUTER);
            if (computers.size() != 2) return false;

            // Проверка изоляции коллекции (R5/R6)
            computers.clear();
            return wh.findByCategory(Category.COMPUTER).size() == 2;
        });

        // 19. TODO 7: Сводка по категориям totalsByCategory
        assertCondition("19. TODO 7: Агрегация totalsByCategory суммирует остатки корректно", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("C-1", "ПК 1", Category.COMPUTER, 15));
            wh.addItem(new StockItem("C-2", "ПК 2", Category.COMPUTER, 25));
            wh.addItem(new StockItem("P-1", "Мышь", Category.PERIPHERAL, 30));

            Map<Category, Integer> totals = wh.totalsByCategory();
            return totals.get(Category.COMPUTER) == 40 &&
                   totals.get(Category.PERIPHERAL) == 30 &&
                   !totals.containsKey(Category.NETWORK);
        });

        // 20. Вариант 4 (Часть Б): addTotalQuantity() экземплярный
        assertCondition("20. Вариант 4 (Часть Б): Экземплярный подсчёт суммы addTotalQuantity()", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("A-1", "Позиция A", Category.COMPUTER, 15));
            wh.addItem(new StockItem("B-1", "Позиция B", Category.DISPLAY, 25));
            wh.addItem(new StockItem("C-1", "Позиция C", Category.NETWORK, 35));
            return wh.addTotalQuantity() == 75;
        });

        // 21. Вариант 4 (Часть Б): addTotalQuantity(Warehouse) статический фасад
        assertCondition("21. Вариант 4 (Часть Б): Статический вызов addTotalQuantity(Warehouse)", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("A-1", "Позиция A", Category.COMPUTER, 10));
            wh.addItem(new StockItem("B-1", "Позиция B", Category.PERIPHERAL, 50));
            return Warehouse.addTotalQuantity(wh) == 60;
        });

        // 22. Вариант 4: Статический вызов с null
        assertThrows("22. Вариант 4: Защита статического addTotalQuantity(null) -> IAE",
                IllegalArgumentException.class,
                () -> Warehouse.addTotalQuantity(null));

        // 23. Инвариант R6: Защита auditLog от несанкционированной очистки извне
        assertCondition("23. Правило R6: Модификация копии getAllMovements не влияет на склад", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("X-1", "Тест", Category.COMPUTER, 20));
            wh.receive("X-1", 5);
            List<StockMovement> copy = wh.getAllMovements();
            copy.clear();
            return wh.movementsCount() == 1;
        });

        // 24. Идентификация сущностей по code: equals и hashCode
        assertCondition("24. Идентичность StockItem по уникальному коду (equals/hashCode)", () -> {
            StockItem it1 = new StockItem("EQ-1", "Версия 1", Category.COMPUTER, 15);
            StockItem it2 = new StockItem("EQ-1", "Версия 2", Category.DISPLAY, 40);
            return it1.equals(it2) && it1.hashCode() == it2.hashCode();
        });

        // 25. Пустой склад: addTotalQuantity возвращает 0
        assertCondition("25. Граничное условие: Пустой склад возвращает сумму 0", () -> {
            Warehouse emptyWh = new Warehouse();
            return emptyWh.addTotalQuantity() == 0 && emptyWh.itemsCount() == 0;
        });

        // 26. Очистка внешнего списка getAllItems не нарушает inventory
        assertCondition("26. Правило R6: Внешняя очистка списка getAllItems() изолирована", () -> {
            Warehouse wh = new Warehouse();
            wh.addItem(new StockItem("Z-1", "Защищенный", Category.NETWORK, 12));
            List<StockItem> all = wh.getAllItems();
            all.clear();
            return wh.itemsCount() == 1;
        });

        System.out.println();
    }

    private static void executeDataDrivenTestPipeline() {
        System.out.println(">>> [ЭТАП 2] Запуск Data-Driven тестирования (data/sample.csv)");

        String[] candidatePaths = {
                "data/sample.csv",
                "practice_9_2/data/sample.csv",
                "../data/sample.csv"
        };

        File csvFile = null;
        for (String p : candidatePaths) {
            File f = new File(p);
            if (f.exists()) {
                csvFile = f;
                break;
            }
        }

        if (csvFile == null) {
            System.out.println("  [ПРЕДУПРЕЖДЕНИЕ] Файл sample.csv не обнаружен по стандартным путям!");
            return;
        }

        Warehouse testWarehouse = new Warehouse();
        int recordsProcessed = 0;
        int successfulImports = 0;
        int rejectedImports = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                recordsProcessed++;
                String[] tokens = line.split(";");
                if (tokens.length < 5) {
                    continue;
                }

                String code = tokens[0].trim();
                String name = tokens[1].trim();
                String catStr = tokens[2].trim();
                String qtyStr = tokens[3].trim();
                String expected = tokens[4].trim();

                boolean isExpectedToSucceed = expected.startsWith("OK");

                try {
                    Category category = parseCategory(catStr);
                    int quantity = Integer.parseInt(qtyStr);
                    StockItem item = new StockItem(code, name, category, quantity);
                    testWarehouse.addItem(item);

                    if (isExpectedToSucceed) {
                        successfulImports++;
                        System.out.printf("  [CSV:OK]  Импортирован '%s' (%s, остаток=%d)\n", code, name, quantity);
                    } else {
                        System.out.printf("  [CSV:ERR] Ожидалась ошибка, но запись добавлена: %s\n", code);
                        testsFailed++;
                    }
                } catch (Exception ex) {
                    if (!isExpectedToSucceed) {
                        rejectedImports++;
                        System.out.printf("  [CSV:DEF] Корректно отклонена некорректная запись '%s': %s\n",
                                code, ex.getMessage());
                    } else {
                        System.out.printf("  [CSV:FAIL] Неожиданное отклонение валидной записи '%s': %s\n",
                                code, ex.getMessage());
                        testsFailed++;
                    }
                }
            }

            System.out.println("  -------------------------------------------------------------");
            System.out.printf("  Итого обработано строк CSV: %d (Успешно: %d, Отклонено: %d)\n",
                    recordsProcessed, successfulImports, rejectedImports);
            System.out.printf("  Итоговое число позиций на складе: %d\n", testWarehouse.itemsCount());
            System.out.printf("  Суммарный запас на складе (addTotalQuantity): %d шт.\n",
                    testWarehouse.addTotalQuantity());
            System.out.println();
        } catch (IOException e) {
            System.out.println("  Ошибка чтения CSV: " + e.getMessage());
        }
    }

    private static Category parseCategory(String value) {
        for (Category c : Category.values()) {
            if (c.name().equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }

    private static void printSummaryReport() {
        System.out.println("================================================================");
        System.out.println("   ИТОГОВЫЙ ОТЧЁТ ВЕРИФИКАЦИИ СИСТЕМЫ");
        System.out.println("================================================================");
        System.out.printf("   Успешно пройдено проверок: %d\n", testsPassed);
        System.out.printf("   Обнаружено сбоев:          %d\n", testsFailed);
        if (testsFailed == 0) {
            System.out.println("   СТАТУС: ВСЕ ТЕСТОВЫЕ ИНВАРИАНТЫ УСПЕШНО ВЫПОЛНЕНЫ (100% PASS)");
        } else {
            System.out.println("   СТАТУС: ОБНАРУЖЕНЫ ОШИБКИ ПРИ ВЕРИФИКАЦИИ!");
        }
        System.out.println("================================================================\n");
    }

    private static void assertCondition(String title, TestSupplier condition) {
        try {
            if (condition.run()) {
                System.out.printf("  [PASS] %s\n", title);
                testsPassed++;
            } else {
                System.out.printf("  [FAIL] %s - условие вернуло false\n", title);
                testsFailed++;
            }
        } catch (Throwable t) {
            System.out.printf("  [FAIL] %s - исключение: %s\n", title, t.getMessage());
            testsFailed++;
        }
    }

    private static void assertThrows(String title, Class<? extends Throwable> expected, TestAction action) {
        try {
            action.run();
            System.out.printf("  [FAIL] %s - ожидалось %s, но исключение не возникло\n",
                    title, expected.getSimpleName());
            testsFailed++;
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                System.out.printf("  [PASS] %s -> %s\n", title, t.getClass().getSimpleName());
                testsPassed++;
            } else {
                System.out.printf("  [FAIL] %s - неверный тип исключения: %s (ожидался %s)\n",
                        title, t.getClass().getSimpleName(), expected.getSimpleName());
                testsFailed++;
            }
        }
    }

    @FunctionalInterface
    interface TestSupplier {
        boolean run() throws Exception;
    }

    @FunctionalInterface
    interface TestAction {
        void run() throws Exception;
    }

    private static void startInteractiveConsole() {
        Scanner scanner = new Scanner(System.in);
        Warehouse warehouse = new Warehouse();

        // Первичное наполнение демонстрационными данными
        warehouse.addItem(new StockItem("WS-01", "Рабочая станция инженера", Category.COMPUTER, 15));
        warehouse.addItem(new StockItem("MN-01", "Монитор 27' IPS", Category.DISPLAY, 20));
        warehouse.addItem(new StockItem("KB-01", "Клавиатура механическая", Category.PERIPHERAL, 25));
        warehouse.addItem(new StockItem("SW-01", "Коммутатор 24 порта", Category.NETWORK, 12));

        System.out.println("================================================================");
        System.out.println("   ИНТЕРАКТИВНОЕ МЕНЮ ДЕМОНСТРАЦИИ СКЛАДСКОГО УЧЁТА");
        System.out.println("   (Вариант 4: неснижаемый запас >= 10, вычисление суммы)");
        System.out.println("================================================================");

        while (true) {
            System.out.println("\nДоступные команды:");
            System.out.println(" 1. Просмотр всех позиций склада (getAllItems)");
            System.out.println(" 2. Оприходование оборудования (receive)");
            System.out.println(" 3. Списание оборудования с контролем резерва 10 (issue)");
            System.out.println(" 4. Расчёт суммарного количества оборудования (addTotalQuantity)");
            System.out.println(" 5. Сводка остатков по категориям (totalsByCategory)");
            System.out.println(" 6. Журнал складских перемещений (getAllMovements)");
            System.out.println(" 0. Завершить работу");
            System.out.print("Выберите действие > ");

            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                System.out.println("Выход из демонстрационного режима.");
                break;
            }

            try {
                switch (choice) {
                    case "1" -> {
                        System.out.println("\n--- ТЕКУЩИЕ ПОЗИЦИИ СКЛАДА ---");
                        warehouse.getAllItems().forEach(it ->
                                System.out.printf("  Код: %-7s | %-28s | %-11s | Остаток: %d\n",
                                        it.getCode(), it.getName(), it.getCategory(), it.getQuantity()));
                    }
                    case "2" -> {
                        System.out.print("Введите инвентарный код: ");
                        String code = scanner.nextLine().trim();
                        System.out.print("Введите количество к поступлению: ");
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        warehouse.receive(code, qty);
                        System.out.printf("Успешно оприходовано %d ед. для [%s]. Новый остаток: %d\n",
                                qty, code, warehouse.getItem(code).getQuantity());
                    }
                    case "3" -> {
                        System.out.print("Введите инвентарный код: ");
                        String code = scanner.nextLine().trim();
                        System.out.print("Введите количество к списанию: ");
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        warehouse.issue(code, qty);
                        System.out.printf("Успешно списано %d ед. для [%s]. Новый остаток: %d\n",
                                qty, code, warehouse.getItem(code).getQuantity());
                    }
                    case "4" -> {
                        int total = warehouse.addTotalQuantity();
                        int staticTotal = Warehouse.addTotalQuantity(warehouse);
                        System.out.printf("\nОбщее количество единиц оборудования на складе: %d (метод экземпляра)\n", total);
                        System.out.printf("Проверка через статический метод Warehouse.addTotalQuantity: %d\n", staticTotal);
                    }
                    case "5" -> {
                        System.out.println("\n--- РАСПРЕДЕЛЕНИЕ ПО КАТЕГОРИЯМ ---");
                        warehouse.totalsByCategory().forEach((cat, count) ->
                                System.out.printf("  Категория %-12s: %d единиц\n", cat, count));
                    }
                    case "6" -> {
                        System.out.println("\n--- ЖУРНАЛ ДВИЖЕНИЙ ОФОРМЛЕННЫХ ОПЕРАЦИЙ ---");
                        warehouse.getAllMovements().forEach(m ->
                                System.out.printf("  [%s] Позиция: %-7s | Количество: %d\n",
                                        m.type(), m.itemCode(), m.quantity()));
                    }
                    default -> System.out.println("Неизвестная команда, повторите ввод.");
                }
            } catch (Exception ex) {
                System.out.println("  [ОШИБКА ОПЕРАЦИИ]: " + ex.getMessage());
            }
        }
    }
}
