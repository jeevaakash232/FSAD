package com.syncengine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Value("${telegram.bot.username}")
    private String botUsername;
    
    @Value("${telegram.bot.chatid}")
    private String chatId;
    
    @Override
    public String getBotUsername() {
        return botUsername;
    }
    
    @Override
    public String getBotToken() {
        return botToken;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        // Handle incoming messages if needed
    }
    
    public void sendAlert(String systemName, Double value, Double threshold, String severity) {
        String emoji = getSeverityEmoji(severity);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(emoji + " " + severity + " ALERT " + emoji + "\n\n" +
                       "System: " + systemName + "\n" +
                       "Event Value: " + value + "\n" +
                       "Threshold: " + threshold + "\n" +
                       "Severity: " + severity + "\n\n" +
                       "Immediate attention required!");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private String getSeverityEmoji(String severity) {
        switch (severity) {
            case "INFO": return "ℹ️";
            case "WARNING": return "⚠️";
            case "ALERT": return "🚨";
            case "CRITICAL": return "🔴";
            default: return "📢";
        }
    }
}
