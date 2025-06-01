package net.strbasic.coursework.controller;

import lombok.RequiredArgsConstructor;
import net.strbasic.coursework.model.ClientRequest;
import net.strbasic.coursework.service.ClientRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QueueController {

    private final ClientRequestService requestService;

    @GetMapping("/queue")
    public String userQueue(@RequestParam(name = "onlyActive", required = false, defaultValue = "false") boolean onlyActive,
                            Model model) {
        List<ClientRequest> requests = requestService.getRequestsByUser(onlyActive);
        model.addAttribute("requests", requests);
        model.addAttribute("onlyActive", onlyActive); // для чекбокса
        return "queue";
    }

    }
