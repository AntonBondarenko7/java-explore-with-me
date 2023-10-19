package ru.practicum.ewm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public class StatsClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

//    private final Gson gson = getGson();
    ObjectMapper objectMapper = new ObjectMapper();

    public StatsClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public EndpointHit saveHit(EndpointHit endpointHitDto) {
        URI uri = URI.create(serverUrl + "/hit");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(endpointHitDto.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(body)
                .build();
        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), EndpointHit.class);
        } catch (NullPointerException | IOException | InterruptedException e) {
            throw new ClientException(request.toString());
        }
    }

    public List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        URI uri = URI.create(serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("start", "start")
                .header("end", "end")
                .header("uris", "uris")
                .header("unique", "unique")
                .GET()
                .build();
        String key = "";
        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            TypeReference<List<ViewStats>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(response.body(), typeRef);
        } catch (NullPointerException | IOException | InterruptedException e) {
            throw new ClientException(request.toString());
        }

    }

}
