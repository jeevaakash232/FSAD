package com.syncengine.config;

import com.syncengine.service.TelegramBotService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    
    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotService telegramBotService) {
        TelegramBotsApi botsApi = null;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return botsApi;
    }
}
