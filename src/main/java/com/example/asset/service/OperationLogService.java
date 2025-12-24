package com.example.asset.service;

import com.example.asset.entity.OperationLog;
import com.example.asset.repository.OperationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository logRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String module, String type, String content) {
        OperationLog log = new OperationLog();
        log.setModule(module);
        log.setOperationType(type);
        log.setContent(content);
        logRepository.save(log);
    }

    public Page<OperationLog> findAll(Pageable pageable) {
        return logRepository.findAll(pageable);
    }

    @Transactional
    public void delete(Long id) {
        logRepository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        logRepository.deleteAllById(ids);
    }
}
