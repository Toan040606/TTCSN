package vn.edu.haui.bachmai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.haui.bachmai.model.NotificationLog;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findTop100ByOrderByCreatedAtDesc();
}
