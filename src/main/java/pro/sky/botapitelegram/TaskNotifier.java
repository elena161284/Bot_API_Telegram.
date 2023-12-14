package pro.sky.botapitelegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import io.swagger.v3.oas.annotations.servers.Server;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.botapitelegram.model.NotificationTask;

import pro.sky.botapitelegram.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@EnableScheduling
public class TaskNotifier {
    private final static Logger logger= LoggerFactory.getLogger(TaskNotifier.class);
    private final TelegramBot bot;
    private final NotificationRepository repository;

    public TaskNotifier(TelegramBot bot, NotificationRepository repository) {
        this.bot = bot;
        this.repository = repository;
    }
    //@Scheduled(cron = "0 0/1 * * * *")
    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedDelay = 1)
    public  void notifyTask() {
        repository.findAllByDateTime(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
        .forEach(notificationTask -> {
            bot.execute(new SendMessage(notificationTask.getChatId(), notificationTask.getText()));
            repository.delete(notificationTask);
            logger.info("notification has been sent!");
        });
    }
}
