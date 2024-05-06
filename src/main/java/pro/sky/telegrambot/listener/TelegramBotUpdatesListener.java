package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Notification_task;
import pro.sky.telegrambot.repository.NTRepository;

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
    private static final String SEND = "/send";

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
                List<Notification_task> tasks = repository.findByDataTime(currentMinute);

                for (Notification_task task : tasks) {
                    sendMessage(task.getChatId(), task.getText());
                }

                switch (message) {
                    case START:
                        startCommand(chatId, message);
                        logger.info("Вызвана команда - /start");
                        break;
                    case SEND:
                        String[] parts = message.split("\\s", 3); // Разбиваем сообщение на три части
                        if (parts.length == 3) {
                            parseAndSaveReminder(chatId, parts[1] + " " + parts[2]);
                            logger.info("Вызвана команда - /send");
                        } else {
                            sendMessage(chatId, "Некорректный формат команды /send");
                            logger.info("Не сохранилось!");
                        }
                        break;
                    case HELP:
                        helpCommand(chatId);
                        logger.info("Вызвана кмоанда - /help");
                        break;
                    default: defaultMessage(chatId);
                        logger.info("Вызван ? Вызвана - неизвествная команда/текст");
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void parseAndSaveReminder(long chatId,String message) {
        Pattern pattern = Pattern.compile("[0-9.:\\s]{16}(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String data = matcher.group(0);
            String text = matcher.group(2);

            LocalDateTime dataTime = LocalDateTime.parse
                    (data, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            Notification_task notification = new Notification_task();
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

    @Scheduled(cron = "0 * * * * *")
    public void processNotifications() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Notification_task> tasks = repository.findByDataTime(currentMinute);

        for (Notification_task task : tasks) {
            sendMessage(task.getChatId(), task.getText());
        }
    }

    // Create commands for TelegramBot

    public void startCommand(long chatId, String userName) {
        String text = "Добро пожаловать в бот , %s! \uD83D\uDE07\n" +
                "\nЭти команды я использую!" +
                "\n /help";

        String formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    public void helpCommand(long chatId) {
        String text = "Справочная по информации команд " +
                "\uD83D\uDC41\u200D\uD83D\uDDE8\n\n/start\n/help";
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