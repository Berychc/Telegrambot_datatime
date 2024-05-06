package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.Notification_task;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface NTRepository extends JpaRepository<Notification_task, Long> {
    List<Notification_task> findByDataTime(LocalDateTime dataTime);
}