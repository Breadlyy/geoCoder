package com.example.geocoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coder")
public class GeoCoderController {
    GeoService geoService;

    @Autowired
    public GeoCoderController(GeoService geoService) {

        this.geoService = geoService;

    }

    @GetMapping("/code")
    public ResponseEntity<String> code(@RequestParam("address") String address) throws JsonProcessingException {
        double time = System.currentTimeMillis();
        String result = geoService.forwardAddress(address);
        System.out.println(System.currentTimeMillis() - time);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/decode")
    public ResponseEntity<String>  decode(@RequestParam("geocode") String geocode,
                       @RequestParam("bbox") String bbox)
    {
        double time = System.currentTimeMillis();
        String result = geoService.decode(geocode, bbox);
        System.out.println(System.currentTimeMillis() - time);
        return ResponseEntity.ok(result);
    }
}
