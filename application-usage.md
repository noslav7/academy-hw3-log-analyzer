# Application Usage

Ниже — практическое руководство, как запускать консольную утилиту `log-analyzer`, какие аргументы она принимает и каких сообщений/кодом завершения ждать при корректном и ошибочном использовании.

## Сборка проекта

Перед запуском CLI необходимо собрать артефакт и (при запуске напрямую) подготовить зависимости:

```powershell
mvn -q test                              # (опционально) убедиться, что все тесты зелёные
mvn -q -DskipTests package               # собрать jar в target/
mvn -q -DskipTests dependency:copy-dependencies -DincludeScope=runtime
```

> После этого приложение можно запускать либо через `mvn exec:java`, либо напрямую через `java -cp` (см. примеры ниже).

## Формат командной строки

```
log-analyzer --path <path-or-url> [...] --format <json|markdown> --output <file> [--from <yyyy-MM-dd>] [--to <yyyy-MM-dd>]
```

- `--path/-p` — один или несколько путей: поддерживаются локальные файлы, glob-шаблоны (`logs/2025*`), а также `http(s)://` URL.
- `--format/-f` — формат результата: `json` или `markdown` (формат `adoc` не реализован и приведёт к ошибке).
- `--output/-o` — файл для сохранения отчёта. Он **должен отсутствовать**, расширение должно соответствовать формату (`.json` или `.md`), директория должна быть доступна на запись.
- `--from`, `--to` — необязательные фильтры дат (формат ISO 8601, `yyyy-MM-dd`). Проверяется, что `from <= to`.

Все ошибки валидации и I/O логируются в stdout/stderr (`log4j`) и завершают процесс кодом `2`. Непредвиденные ошибки — код `1`. Успешное выполнение — код `0`.

## Запуск через Maven (вариант по умолчанию)

```powershell
mvn -q exec:java `
  "-Dexec.mainClass=academy.Application" `
  "-Dexec.args=--path scripts/data/input/logs/part1.txt scripts/data/input/logs/part2.txt --format json --output target/report.json"
```

После выполнения в `target/report.json` появится отчёт, структура и значения соответствуют требованиям README.

## Запуск напрямую через `java`

```powershell
java -cp "target\hw3-logs-1.0.jar;target\dependency\*" academy.Application `
  --path scripts/data/input/logs/part1.txt `
  --format markdown `
  --output target/report.md
```

Файл `target/report.md` — человекочитаемый Markdown-отчёт с таблицами по ресурсам, кодам ответа и пр.

## Примеры входных путей

- Один локальный файл:
  ```powershell
  ... --path logs/access.log ...
  ```
- Глоб-шаблон:
  ```powershell
  ... --path "logs/2025*.log" ...
  ```
- Несколько путей/URL за один запуск:
  ```powershell
  ... --path access.log https://example.com/nginx.log ...
  ```

## Примеры фильтрации по датам

```powershell
mvn -q exec:java `
  "-Dexec.mainClass=academy.Application" `
  "-Dexec.args=--path scripts/data/input/logs/part1.txt --format json --output target/report.json --from 2015-05-17 --to 2015-05-18"
```

Если указать `--from 2015-05-18 --to 2015-05-17`, утилита завершится кодом `2` и выведет:

```
ERROR ... --from must be before or equal to --to
```

## Типовые ошибки и сообщения

| Ситуация                                             | Что происходит                                                                                  |
|------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| Файл по `--path` не найден                           | `ERROR ... File not found: <path>` и код `2`.                                                    |
| URL вернул 404                                       | `ERROR ... Remote file not found (404): <url>` и код `2`.                                        |
| Расширение входного файла не `.log`/`.txt`           | `ERROR ... Unsupported file format ... Supported: [log, txt]` и код `2`.                         |
| Запрошен формат `adoc`                               | `ERROR ... Unsupported format: adoc` и код `2`.                                                  |
| Выходной файл уже существует                         | `ERROR ... Output file already exists: <path>` и код `2`.                                        |
| Указано неизвестное имя опции (например `--input`)   | Picocli печатает usage и сообщение `Unknown options: '--input' ...` + лог `ERROR` + код `2`.     |
| Неверный формат даты                                 | `ERROR ... Invalid value for --from: <value>. Expected ISO-8601 date (yyyy-MM-dd)` + код `2`.    |
| Пустое значение даты (например `--from ""`)          | `ERROR ... Invalid value for --from: value must not be blank` + код `2`.                         |
| Попалась строка лога с некорректным форматом         | `WARN ... Skipped malformed log line ...` (строка пропускается, выполнение продолжается).        |

Все сообщения выводятся log4j в stdout, что позволяет интегрировать утилиту в скрипты и CI/CD.

## Результаты

- JSON-отчёт соответствует схеме из README (`files`, `totalRequestsCount`, `responseSizeInBytes`, `resources`, `responseCodes`, `requestsPerDate`, `uniqueProtocols`).
- Markdown-отчёт содержит разделы «Общая информация», «Запрашиваемые ресурсы», «Коды ответа», «Запросы по датам», «Используемые протоколы».

## Очистка

Удалить временные артефакты можно стандартной командой:

```powershell
mvn clean
del target\report.json, target\report.md
```

— после этого проект готов к повторному запуску.

