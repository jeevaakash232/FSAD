package com.syncengine.service;

import com.syncengine.model.SystemEvent;
import com.syncengine.repository.SystemEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventSyncService {
    
    @Autowired
    private SystemEventRepository eventRepository;
    
    @Autowired
    private TelegramBotService telegramBotService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Value("${alert.threshold.value}")
    private Double defaultThreshold;
    
    @Value("${alert.threshold.smart-home}")
    private Double smartHomeThreshold;
    
    @Value("${alert.threshold.industrial}")
    private Double industrialThreshold;
    
    @Value("${alert.threshold.environmental}")
    private Double environmentalThreshold;
    
    @Value("${alert.threshold.vehicle}")
    private Double vehicleThreshold;
    
    public SystemEvent createEvent(SystemEvent event) {
        // Determine severity level
        String severity = calculateSeverity(event.getSystemName(), event.getEventValue());
        event.setStatus(severity);
        
        SystemEvent savedEvent = eventRepository.save(event);
        
        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/events", savedEvent);
        
        // Check threshold and send Telegram alert for ALERT and CRITICAL
        Double threshold = getThresholdForSystem(event.getSystemName());
        if (event.getEventValue() != null && event.getEventValue() > threshold) {
            telegramBotService.sendAlert(event.getSystemName(), event.getEventValue(), threshold, severity);
        }
        
        return savedEvent;
    }
    
    private String calculateSeverity(String systemName, Double value) {
        if (value == null) return "NORMAL";
        
        Double threshold = getThresholdForSystem(systemName);
        
        if (value < threshold * 0.5) return "INFO";
        else if (value < threshold) return "WARNING";
        else if (value < threshold * 1.5) return "ALERT";
        else return "CRITICAL";
    }
    
    private Double getThresholdForSystem(String systemName) {
        switch (systemName) {
            case "Smart-Home-Monitor": return smartHomeThreshold;
            case "Industrial-Sensor-Network": return industrialThreshold;
            case "Environmental-Monitoring": return environmentalThreshold;
            case "Vehicle-Tracking-System": return vehicleThreshold;
            default: return defaultThreshold;
        }
    }
    
    public List<SystemEvent> getAllEvents() {
        return eventRepository.findTop50ByOrderByTimestampDesc();
    }
    
    public List<SystemEvent> getEventsBySystem(String systemName) {
        return eventRepository.findBySystemNameOrderByTimestampDesc(systemName);
    }
}
