package com.ipinfo.controller;

import com.ipinfo.entity.IpInfo;
import com.ipinfo.repository.IpInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/Ip")
public class IpController {

    private final IpInfoRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public IpController(IpInfoRepository repository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/Add")
    public ResponseEntity<String> addIp(@RequestParam(required = false) String ipAddress) {
        // Basic validation
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("IP address is required");
        }

        // Make API call to fetch IP data
        String url = String.format("https://ipinfo.io/%s/geo", ipAddress);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            String payload = objectMapper.writeValueAsString(response);

            // Prepare entity to store in database
            IpInfo ipInfo = new IpInfo();
            ipInfo.setIpAddress(ipAddress);
            ipInfo.setPayload(payload);

            // Save to database
            repository.save(ipInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body("IP data saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch or save IP data: " + e.getMessage());
        }
    }

    @GetMapping("/Get")
    public ResponseEntity<?> getIp(@RequestParam(required = false) String ipAddress) {
        // Basic validation
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("IP address is required");
        }

        // Check database to see if IP exists
        Optional<IpInfo> ipInfo = repository.findByIpAddress(ipAddress);
        if (ipInfo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No data found for IP address: " + ipAddress);
        }

        // Return payload as JSON
        try {
            Object payload = objectMapper.readValue(ipInfo.get().getPayload(), Object.class);
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to parse payload: " + e.getMessage());
        }
    }
}