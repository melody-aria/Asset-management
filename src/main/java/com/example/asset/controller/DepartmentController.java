package com.example.asset.controller;

import com.example.asset.entity.Location;
import com.example.asset.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    // 复用 LocationService，因为业务上 部门 = 位置
    private final LocationService locationService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Location> pageResult = locationService.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "id")));
        
        model.addAttribute("departments", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("module", "departments");
        return "departments";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Location location) {
        locationService.save(location);
        return "redirect:/departments";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Location location) {
        locationService.save(location);
        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        try {
            locationService.delete(id);
        } catch (Exception e) {
            // 实际项目中可能需要处理外键约束异常（如果位置下有资产）
            return "redirect:/departments?error=hasAssets";
        }
        return "redirect:/departments";
    }

    @PostMapping("/delete/batch")
    public String deleteBatch(@RequestParam("ids") List<Long> ids) {
        try {
            locationService.deleteBatch(ids);
        } catch (Exception e) {
            return "redirect:/departments?error=hasAssets";
        }
        return "redirect:/departments";
    }
}