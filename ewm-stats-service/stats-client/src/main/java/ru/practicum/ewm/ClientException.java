package ru.practicum.ewm;

public class ClientException extends RuntimeException {

    public ClientException(String request) {
        super("Ошибка в клиенте статистики при выполнении запроса: " + request);
    }

}
