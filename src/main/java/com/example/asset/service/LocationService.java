package com.example.asset.service;

import com.example.asset.entity.Location;
import com.example.asset.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final OperationLogService logService;

    public List<Location> findAll() {
        return locationRepository.findAll();
    }
    
    public Page<Location> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    @Transactional
    public Location save(Location location) {
        if (location.getId() != null) {
            logService.log("部门管理", "修改", "修改部门: " + location.getName());
        } else {
            logService.log("部门管理", "新增", "新增部门: " + location.getName());
        }
        return locationRepository.save(location);
    }
    
    @Transactional
    public void delete(Long id) {
        locationRepository.findById(id).ifPresent(loc -> {
            logService.log("部门管理", "删除", "删除部门: " + loc.getName());
            locationRepository.delete(loc);
        });
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Location> locations = locationRepository.findAllById(ids);
        if (!locations.isEmpty()) {
            StringBuilder content = new StringBuilder("批量删除部门: ");
            for (Location loc : locations) {
                content.append(loc.getName()).append(", ");
            }
            if (content.length() > 2) {
                content.setLength(content.length() - 2);
            }
            logService.log("部门管理", "批量删除", content.toString());
            locationRepository.deleteAll(locations);
        }
    }

    public Location findByNameOrCreate(String name) {
        return locationRepository.findByName(name)
                .orElseGet(() -> {
                    Location loc = new Location();
                    loc.setName(name);
                    return locationRepository.save(loc);
                });
    }
}