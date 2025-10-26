# Система управления банковскими картами

## 1. Клонирование репозитория

```bash
git clone https://github.com/VictorNVK/Effective-Mobile-test-task.git
cd Effective-Mobile-test-task
```

## 2. Настройка переменных окружения

Скопируйте файл `example.env` в `.env` и заполните значения:

```bash
cp example.env .env
```

Пример для секретных необходимых параметров:

```env
PAN_ENC_KEY_BASE64=qI7+Msn+KWd5Sgew508opjlRcXTSWyUqW9gulQn1pp0=
JWT_SECRET=uY8p0zS9m4Xk6+6JGZveHFkNjEcNWef39/C4R2tQeM0=
```

Дополнительно настройте параметры подключения к БД и порты по примеру из `example.env`.

## 3. Сборка и запуск через Docker Compose

```bash
docker compose up --build
```

После сборки приложение будет доступно на порту, указанном в `.env` (`SERVER_PORT`, по умолчанию `8901`). Для остановки контейнеров используйте `Ctrl+C`, а затем:

```bash
docker compose down
```

## 4. Swagger UI

Интерактивная документация доступна после запуска сервиса по адресу:

```
http://localhost:8901/api/v3/swagger-ui/index.html#/
```
