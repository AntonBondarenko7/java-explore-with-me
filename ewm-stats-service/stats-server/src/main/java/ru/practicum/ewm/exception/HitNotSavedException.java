package ru.practicum.ewm.exception;

public class HitNotSavedException extends RuntimeException {

    public HitNotSavedException(String endpointHit) {
        super("Информация о запросе не была сохранена: " + endpointHit);
    }

}
