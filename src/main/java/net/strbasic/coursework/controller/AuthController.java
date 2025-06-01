package net.strbasic.coursework.controller;

import lombok.RequiredArgsConstructor;
import net.strbasic.coursework.model.ClientRequest;
import net.strbasic.coursework.model.User;
import net.strbasic.coursework.repository.UserRepository;
import net.strbasic.coursework.service.ClientRequestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRequestService requestService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Ця електронна адреса вже зареєстрована.");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Паролі не співпадають.");
            return "register";
        }

        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .username(email)
                .password(passwordEncoder.encode(password))
                .role("USER")
                .build();

        userRepository.save(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "login";
    }


    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(exists ? "EXISTS" : "OK");
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "service", required = false) String service, Model model) {
        Map<String, List<ClientRequest>> allQueues = requestService.getActiveRequestsByServiceType();
        model.addAttribute("services", allQueues.keySet());
        model.addAttribute("selectedService", service);

        if (service != null && allQueues.containsKey(service)) {
            model.addAttribute("requests", allQueues.get(service));
        } else {
            model.addAttribute("requests", List.of());
        }

        return "dashboard";
    }

    @PostMapping("/auth/queue/add")
    public String addRequest(
            @RequestParam("serviceType") String serviceType,
            @RequestParam("appointmentTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime,
            RedirectAttributes redirectAttributes
    ) {
        try {
            requestService.addRequest(serviceType, appointmentTime);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/queue";
    }


}