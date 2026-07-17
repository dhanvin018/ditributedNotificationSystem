package com.notificationapp.eventposting;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/events")
public class EventRestController {

    private final EventPublisherService eventPublisherService;

    public EventRestController(EventPublisherService eventPublisherService) {
        this.eventPublisherService = eventPublisherService;
    }

    @PostMapping
    public ResponseEntity<String> postEvent(@RequestBody @Valid EventRequest request)
            throws ExecutionException, InterruptedException {
        String result = eventPublisherService.publish(request.email(), request.message());
        return ResponseEntity.ok(result);
    }
}