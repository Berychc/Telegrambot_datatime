package project.bot.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import project.bot.telegrambot.model.NotificationTask;
import project.bot.telegrambot.repository.NTRepository;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    // Create a new constants for TelegramBot
    private static final String START = "/start";
    private static final String HELP = "/help";

    @Autowired
    private TelegramBot bot;

    @Autowired
    private NTRepository repository;

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null) {
                String message = update.message().text();
                long chatId = update.message().chat().id();

                LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                List<NotificationTask> tasks = repository.findByDataTime(currentMinute);

                for (NotificationTask task : tasks) {
                    sendMessage(task.getChatId(), task.getText());
                }

                switch (message) {
                    case START:
                        startCommand(chatId, message);
                        logger.info("Вызвана команда - /start");
                        break;
                    case HELP:
                        helpCommand(chatId);
                        logger.info("Вызвана кмоанда - /help");
                        break;
                    default: parseAndSaveReminder(chatId, message);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void parseAndSaveReminder(long chatId,String message) {
        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String data = matcher.group(1);
            String text = matcher.group(3);

            LocalDateTime dataTime = LocalDateTime.parse(data, DateTimeFormatter.ofPattern
                    ("dd.MM.yyyy HH:mm"));

            NotificationTask notification = new NotificationTask();
            notification.setDataTime(dataTime);
            notification.setText(text);
            notification.setChatId(chatId);
            repository.save(notification);

            sendMessage(chatId, "Новое уведомление успешно добавлено!");
            logger.info("Успешно добавленно!");
        } else {
            logger.info("Не найдено совпадений по шаблону в сообщении: {}", message);
            sendMessage(chatId, "Произошла ошибка при добавлении уведомления.");
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void processNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findByDataTime(currentMinute);

        for (NotificationTask task : tasks) {
            sendMessage(task.getChatId(), task.getText());
        }
    }

    // Create commands for TelegramBot

    public void startCommand(long chatId, String userName) {
        String text = "Добро пожаловать в бот! \n" +
                "\nИспользуйте команду - /help для дальнейшей инструкции!";

        String formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    public void helpCommand(long chatId) {
        String text = "Чтобы поставить напоминание используйте шаблон :" +
                "\n Пример - 22.06.2001 16:00 У вас день рождение " +
                "\uD83D\uDC41\u200D\uD83D\uDDE8";
        sendMessage(chatId, text);
    }

    public void defaultMessage(long chatId) {
        sendMessage(chatId, "Не удалось распознать команду \uD83D\uDE48");
    }

    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        try {
            bot.execute(sendMessage);
        } catch (Exception e) {
            logger.error("Error sending message : {}", e.getMessage());
        }
    }
}