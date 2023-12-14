package pro.sky.botapitelegram;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.LoggerFactory;
import pro.sky.botapitelegram.model.NotificationTask;
import pro.sky.botapitelegram.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.hibernate.internal.CoreLogging.logger;

public final class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final DateTimeFormatter DATE_TIME_PATTERN=DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
    private static final Pattern PATTERN= Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private final TelegramBot bot;
    private final NotificationRepository repository;

    public TelegramBotUpdatesListener(TelegramBot bot, NotificationRepository repository) {
        this.bot = bot;
        this.repository=repository;
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            var text = update.message().text();
            var chatId=update.message().chat().id();

            if ("/start".equals(text)) {
                bot.execute(new SendMessage(chatId, "Добро пожаловать в бот!"));
            } else {
                //01.01.2022 20:00 Сделать домашнюю работу
                var matcher=PATTERN.matcher(text);
                if (matcher.matches()) { // если подошло то,...
                    LocalDateTime dateTime=parseTime(matcher.group(1));
                    if (dateTime == null) {
                        bot.execute(new SendMessage(chatId,"Формат даты не верный!"));// формат даты не верный
                        continue; //переходим на следующую итерацию цикла
                    }
                    var taskText=matcher.group(3);

                    NotificationTask task =new NotificationTask();
                    task.setChatId(chatId);
                    task.setText(taskText);
                    task.setDateTime(dateTime);
                    NotificationTask saved=repository.save(task);
                    sendMessage(chatId,"Задача запланирована!");
                    logger.info("Notification task save: {}", saved);
                }

            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    private  void sendMessage(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text));
    }
    private LocalDateTime parseTime(String text) {
        try {
            return LocalDateTime.parse(text, DATE_TIME_PATTERN);
        } catch (DateTimeParseException e) {// если текст не нужного формата, то вылезет ошибка
            logger.error("Cannot parse date and time:{}", text);
        }
        return null;
    }
}