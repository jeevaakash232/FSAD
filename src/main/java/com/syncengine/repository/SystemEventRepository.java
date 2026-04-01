package com.syncengine.repository;

import com.syncengine.model.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SystemEventRepository extends JpaRepository<SystemEvent, Long> {
    List<SystemEvent> findBySystemNameOrderByTimestampDesc(String systemName);
    List<SystemEvent> findTop50ByOrderByTimestampDesc();
}
