package com.example.asset.service;

import com.example.asset.entity.Manager;
import com.example.asset.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final OperationLogService logService;

    public List<Manager> findAll() {
        return managerRepository.findAll();
    }
    
    public Page<Manager> findAll(Pageable pageable) {
        return managerRepository.findAll(pageable);
    }

    public Optional<Manager> findById(Long id) {
        return managerRepository.findById(id);
    }

    public Optional<Manager> findByName(String name) {
        return managerRepository.findByName(name);
    }
    
    @Transactional
    public Manager findByNameOrCreate(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return managerRepository.findByName(name)
                .orElseGet(() -> {
                    Manager newManager = new Manager();
                    newManager.setName(name);
                    return managerRepository.save(newManager);
                });
    }

    @Transactional
    public void save(Manager manager) {
        if (manager.getId() != null) {
            logService.log("管理员管理", "修改", "修改管理员: " + manager.getName());
        } else {
            logService.log("管理员管理", "新增", "新增管理员: " + manager.getName());
        }
        managerRepository.save(manager);
    }

    @Transactional
    public void delete(Long id) {
        managerRepository.findById(id).ifPresent(mgr -> {
            logService.log("管理员管理", "删除", "删除管理员: " + mgr.getName());
            managerRepository.delete(mgr);
        });
    }
    
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Manager> managers = managerRepository.findAllById(ids);
        if (!managers.isEmpty()) {
            StringBuilder content = new StringBuilder("批量删除管理员: ");
            for (Manager mgr : managers) {
                content.append(mgr.getName()).append(", ");
            }
            if (content.length() > 2) {
                content.setLength(content.length() - 2);
            }
            logService.log("管理员管理", "批量删除", content.toString());
            managerRepository.deleteAll(managers);
        }
    }
}
