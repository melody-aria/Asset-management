package com.example.asset.controller;

import com.example.asset.entity.Manager;
import com.example.asset.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
@RequestMapping("/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Manager> pageResult = managerService.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "id")));
        
        model.addAttribute("managers", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("module", "managers");
        return "managers";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Manager manager) {
        managerService.save(manager);
        return "redirect:/managers";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Manager manager) {
        managerService.save(manager);
        return "redirect:/managers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        managerService.delete(id);
        return "redirect:/managers";
    }

    @PostMapping("/delete/batch")
    public String deleteBatch(@RequestParam("ids") List<Long> ids) {
        managerService.deleteBatch(ids);
        return "redirect:/managers";
    }
}
