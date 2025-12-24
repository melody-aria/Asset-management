package com.example.asset.controller;

import com.example.asset.entity.OperationLog;
import com.example.asset.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService logService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<OperationLog> pageResult = logService.findAll(PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "operateTime")));
        
        model.addAttribute("logs", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("module", "logs");
        return "logs";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        logService.delete(id);
        return "redirect:/logs";
    }

    @PostMapping("/delete/batch")
    public String deleteBatch(@RequestParam("ids") List<Long> ids) {
        logService.deleteBatch(ids);
        return "redirect:/logs";
    }
}
