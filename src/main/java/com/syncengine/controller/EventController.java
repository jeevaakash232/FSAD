package com.syncengine.controller;

import com.syncengine.model.SystemEvent;
import com.syncengine.service.EventSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
    
    @Autowired
    private EventSyncService eventSyncService;
    
    @PostMapping
    public ResponseEntity<SystemEvent> createEvent(@RequestBody SystemEvent event) {
        return ResponseEntity.ok(eventSyncService.createEvent(event));
    }
    
    @GetMapping
    public ResponseEntity<List<SystemEvent>> getAllEvents() {
        return ResponseEntity.ok(eventSyncService.getAllEvents());
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<java.util.Map<String, Object>> getStatistics() {
        List<SystemEvent> allEvents = eventSyncService.getAllEvents();
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalEvents", allEvents.size());
        stats.put("infoCount", allEvents.stream().filter(e -> "INFO".equals(e.getStatus())).count());
        stats.put("warningCount", allEvents.stream().filter(e -> "WARNING".equals(e.getStatus())).count());
        stats.put("alertCount", allEvents.stream().filter(e -> "ALERT".equals(e.getStatus())).count());
        stats.put("criticalCount", allEvents.stream().filter(e -> "CRITICAL".equals(e.getStatus())).count());
        
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/system/{systemName}")
    public ResponseEntity<List<SystemEvent>> getEventsBySystem(@PathVariable("systemName") String systemName) {
        return ResponseEntity.ok(eventSyncService.getEventsBySystem(systemName));
    }

}
