package project.bot.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.bot.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface NTRepository extends JpaRepository<NotificationTask, Long> {
    List<NotificationTask> findByDataTime(LocalDateTime dataTime);
}