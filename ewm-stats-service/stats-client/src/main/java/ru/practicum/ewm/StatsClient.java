package ru.practicum.ewm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class StatsClient {

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final String serverUrl;

    ObjectMapper objectMapper = new ObjectMapper();

    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public EndpointHit saveHit(EndpointHit endpointHitDto) {
        URI uri = URI.create(serverUrl + "/hit");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(endpointHitDto.toString());
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
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

    public List<ViewStats> getAllStats(String start, String end,
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
