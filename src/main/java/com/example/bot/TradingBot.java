package com.example.bot;


import com.example.client.AlphaVantageApiClient;
import com.example.client.BingApiClient;
import com.example.pojo.AlphaVantageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.example.graph.GraphBuilder.buildGraph;


public class TradingBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TradingBot.class);
    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private boolean chatIsActive;

    public TradingBot() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            Properties props = new Properties();
            props.load(input);
            BOT_NAME = props.getProperty("botName");
            BOT_TOKEN = props.getProperty("botToken");
        } catch (NullPointerException | IOException e) {
            logger.error("Unable to read properties from the configuration file in the resources section", e);
            throw new NullPointerException();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            if (messageText.equals("/start")) {
                startChat(update);
            } else if (chatIsActive) {
                sendRequestAboutValueOfTheCompanyShares(update);
            } else {
                sendWarningMessage(update);
            }
        }
    }

    private void startChat(Update update) {
        String userName = update.getMessage().getChat().getFirstName();
        String greeting = "Hello, " + userName + "! \nI can build a graph of the company's stock price."
                + "\nPlease enter the name of the company:";
        sendMessageToChat(update, greeting);
        chatIsActive = true;
    }

    private void sendMessageToChat(Update update, String message) {
        try {
            execute(new SendMessage(update.getMessage().getChatId().toString(), message));
        } catch (TelegramApiException e) {
            logger.error("Unable to send message to chat", e);
        }
    }

    private void sendWarningMessage(Update update) {
        String warning = "Please enter the \"/start\" to begin.";
        sendMessageToChat(update, warning);
    }

    private void sendRequestAboutValueOfTheCompanyShares(Update update) {
        if (!update.getMessage().getText().equals("/start")) {
            String company = update.getMessage().getText();
            String message = "I'm looking for information about " + company;
            sendMessageToChat(update, message);

            String companyNameInEnglish = getCompanyNameInEnglish(company);
            String companySTicker = getCompanySTicker(companyNameInEnglish);

            if (companySTicker != null){
                message = "One moment, I am drawing a graph for " + companySTicker;
                sendMessageToChat(update, message);
                AlphaVantageResponse response = getStockData(companySTicker);
                sendGraph(update, response);
            }else {
                message = "We apologize, but it seems that you entered the incorrect company name, " +
                        "or on Alpha Vantage there is no information about the value of the shares of " + companyNameInEnglish
                        + "\nPlease enter the name of the company:";
                sendMessageToChat(update, message);
            }
        }
    }

    private String getCompanyNameInEnglish(String company) {
        BingApiClient bingApiClient = new BingApiClient();
        return bingApiClient.getCorrectCompanyNameInEnglish(company);
    }

    private String getCompanySTicker(String companyNameInEnglish) {
        AlphaVantageApiClient alphaVantageApiClient = new AlphaVantageApiClient();
        return alphaVantageApiClient.getTickerSymbol(companyNameInEnglish);
    }

    private AlphaVantageResponse getStockData(String companySTicker) {
        AlphaVantageApiClient alphaVantageApiClient = new AlphaVantageApiClient();
        return alphaVantageApiClient.getStockData(companySTicker);
    }


    private void sendGraph(Update update, AlphaVantageResponse response) {
        // Use the Telegram Bot API's sendPhoto method to send the graph as a photo to your bot
        SendPhoto graph = new SendPhoto(update.getMessage().getChatId().toString(), buildGraph(response));
        graph.setCaption("Share price graph");
        try {
            execute(graph);
            chatIsActive = false;
        } catch (TelegramApiException e) {
            logger.error("Unable to send a graph to a chat to a user", e);
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}

