package project.bot.telegrambot.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notificationTask")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "text")
    private String text;

    @Column(name = "data_time")
    private LocalDateTime dataTime;

    public NotificationTask() {
    }

    public NotificationTask(Long id, Long chatId, String text, LocalDateTime dataTime) {
        this.id = id;
        this.chatId = chatId;
        this.text = text;
        this.dataTime = dataTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDataTime() {
        return dataTime;
    }

    public void setDataTime(LocalDateTime dataTime) {
        this.dataTime = dataTime;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NotificationTask that = (NotificationTask) object;
        return Objects.equals(id, that.id) && Objects.equals(chatId, that.chatId) && Objects.equals(text, that.text) && Objects.equals(dataTime, that.dataTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, text, dataTime);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", text='" + text + '\'' +
                ", dataTime=" + dataTime +
                '}';
    }
}