package academy.formatter;

import academy.stats.StatsResult;

/** Форматирует агрегированную статистику в текстовое представление, готовое для записи в результирующий файл. */
public interface StatsFormatter {

    /**
     * Преобразует вычисленную статистику в строку нужного формата.
     *
     * @param statsResult итоговые метрики, собранные из логов
     * @return сериализованное представление статистики
     */
    String format(StatsResult statsResult);
}
