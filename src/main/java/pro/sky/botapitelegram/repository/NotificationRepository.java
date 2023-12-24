package pro.sky.botapitelegram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.sky.botapitelegram.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findAllByDateTime(LocalDateTime dateTime);
}
