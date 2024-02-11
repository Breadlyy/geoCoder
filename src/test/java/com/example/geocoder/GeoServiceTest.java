package com.example.geocoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@SpringBootTest
public class GeoServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private GeoService geoService;

    @Test
    public void testForwardAddress() throws JsonProcessingException {
        // Arrange
        String address = "Some Address";
        String cachedResult = "{\"response\": {\"GeoObjectCollection\": {\"featureMember\": [{\"GeoObject\": {\"boundedBy\": {\"Envelope\": {\"lowerCorner\": \"1.23,4.56\", \"upperCorner\": \"7.89,10.11\"}}, \"Point\": {\"pos\": \"12.34 56.78\"}}}]} }}";
        String expectedResult = "lowerCorner: 1.23,4.56 \nupperCorner: 7.89,10.11 \npos: 12.34 56.78";

        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        valueOperations.set(anyString(), anyString());

        WebClient webClient = mock(WebClient.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono<String> mono = Mono.just(cachedResult);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri((URI) any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);

        // Act
        String result = geoService.forwardAddress(address);

        // Assert
        assert result.equals(expectedResult);
        verify(redisTemplate, times(1)).opsForValue().get(anyString());
        verify(redisTemplate, times(1)).opsForValue().set(anyString(), anyString());
    }
}
