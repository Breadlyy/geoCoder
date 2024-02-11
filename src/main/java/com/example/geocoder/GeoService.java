package com.example.geocoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeoService{
    private static final Logger log = LoggerFactory.getLogger(GeoService.class);

    @Value("${geocoding.api.key}")
    private String geocodingApiUrl;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public GeoService() {
    }


    public String forwardAddress(String address) throws JsonProcessingException {
        String cacheKey = "code_" + address;
        String cacheResult = redisTemplate.opsForValue().get(cacheKey);
        if(cacheResult != null)
        {
           return extractTheCoordinates(cacheResult);
        }
        else {
            WebClient webClient = WebClient.builder().baseUrl(geocodingApiUrl).build();
            String result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("geocode", address)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            redisTemplate.opsForValue().set(cacheKey, result);
           return extractTheCoordinates(result);

        }
    }
    public String decode(String geoCode, String bbox) {
        String cacheKey = "decode_" + geoCode + "_" + bbox;
        String cachedResult = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
           return extractTheAddress(cachedResult);
        } else {
            String[] str = bbox.split("~");
            String lowerCorner = str[0];
            String upperCorner = str[1];
            WebClient webClient = WebClient.builder().baseUrl(geocodingApiUrl).build();
            String result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("geocode", geoCode)
                            .queryParam("lowerCorner", lowerCorner)
                            .queryParam("upperCorner", upperCorner)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            redisTemplate.opsForValue().set(cacheKey, result);
          return   extractTheAddress(result);
        }
    }

    public String extractTheAddress(String jsonResponse) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode featureMembers = root.path("response")
                    .path("GeoObjectCollection")
                    .path("featureMember");

            JsonNode geoObject = featureMembers.get(0).path("GeoObject");

            JsonNode metaDataProperty = geoObject.path("metaDataProperty")
                    .path("GeocoderMetaData");

            JsonNode components = metaDataProperty.path("Address")
                    .path("Components");

            // Вывести значения из массива "Components"
            for (JsonNode component : components) {
                String kind = component.path("kind").asText();
                String name = component.path("name").asText();
                stringBuilder.append(name + " ");
                log.info("Kind: " + kind);
                log.info("Name: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("extractTheAddress method's result: " + stringBuilder.toString());
        return stringBuilder.toString();
    }
    public String extractTheCoordinates(String jsonString) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonString);


        JsonNode envelope = jsonNode.path("response").path("GeoObjectCollection").path("featureMember").get(0)
                .path("GeoObject").path("boundedBy").path("Envelope");
        String lowerCorner = envelope.path("lowerCorner").asText();
        String upperCorner = envelope.path("upperCorner").asText();

        String pos = jsonNode.path("response").path("GeoObjectCollection").path("featureMember").get(0)
                .path("GeoObject").path("Point").path("pos").asText();

        StringBuilder stringBuilder = new StringBuilder(String
                .format("lowerCorner: %s \nupperCorner: %s \npos: %s",
                        lowerCorner, upperCorner, pos));
        log.info("lowerCorner: " + lowerCorner);
        log.info("upperCorner: " + upperCorner);
        log.info("pos: " + pos);
        return stringBuilder.toString();
    }

}
