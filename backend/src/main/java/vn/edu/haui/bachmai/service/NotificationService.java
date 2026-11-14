package vn.edu.haui.bachmai.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.haui.bachmai.model.*;
import vn.edu.haui.bachmai.repository.*;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationLogRepository notifications;
    private final AppointmentRepository appointments;
    @Value("${app.notification-provider:LOG}")
    private String provider;

    @Transactional
    public void bookingConfirmed(Appointment a) {
        String text = "Bệnh viện Bạch Mai xác nhận lịch " + a.getCode() + " vào " + a.getAppointmentTime() +
            " ngày " + a.getAppointmentDate() + ". Vui lòng đến trước 15 phút.";
        queue(a, "SMS", "BOOKING_CONFIRMED", a.getPatient().getPhone(), text);
        if (a.getPatient().getEmail() != null && !a.getPatient().getEmail().isBlank())
            queue(a, "EMAIL", "BOOKING_CONFIRMED", a.getPatient().getEmail(), text);
    }

    @Transactional
    public void bookingChanged(Appointment a) {
        queue(a, "SMS", "BOOKING_CHANGED", a.getPatient().getPhone(),
            "Lịch " + a.getCode() + " đã đổi sang " + a.getAppointmentTime() + " ngày " + a.getAppointmentDate() + ".");
    }

    @Transactional
    public void bookingCancelled(Appointment a) {
        queue(a, "SMS", "BOOKING_CANCELLED", a.getPatient().getPhone(), "Lịch " + a.getCode() + " đã được hủy.");
    }

    @Scheduled(cron = "${app.reminder-cron:0 0 8 * * *}")
    @Transactional
    public void sendTomorrowReminders() {
        appointments.findByAppointmentDateAndStatusIn(LocalDate.now().plusDays(1),
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)).stream()
            .filter(a -> a.getReminderSentAt() == null).forEach(a -> {
                queue(a, "SMS", "APPOINTMENT_REMINDER", a.getPatient().getPhone(),
                    "Nhắc lịch khám " + a.getCode() + " lúc " + a.getAppointmentTime() + " ngày mai tại " + a.getFacility().getName() + ".");
                a.setReminderSentAt(LocalDateTime.now());
            });
    }

    private void queue(Appointment a, String channel, String type, String recipient, String content) {
        NotificationLog item = notifications.save(NotificationLog.builder().appointment(a).channel(channel).type(type)
            .recipient(recipient).content(content).status("QUEUED").build());
        // LOG là provider demo; adapter SMTP/SMS thật có thể thay tại đây mà không đổi nghiệp vụ outbox.
        if ("LOG".equalsIgnoreCase(provider)) {
            log.info("Notification delivered by LOG id={} channel={} type={} recipient={} content={}",
                item.getId(), channel, type, recipient, content);
            item.setStatus("SENT");
            item.setSentAt(LocalDateTime.now());
        }
    }
}
