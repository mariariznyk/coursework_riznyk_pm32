package net.strbasic.coursework.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.strbasic.coursework.model.ClientRequest;
import net.strbasic.coursework.model.User;
import net.strbasic.coursework.repository.ClientRequestRepository;
import net.strbasic.coursework.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientRequestService {

    private final ClientRequestRepository repository;
    private final UserRepository userRepository;

    public List<ClientRequest> getAllRequests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findAllByUsernameOrderByIdAsc(email);
    }

    public void addRequest(String serviceType, LocalDateTime appointmentTime) {
        int hour = appointmentTime.getHour();
        if (hour < 8 || hour >= 18) {
            throw new RuntimeException("Запис можливий лише з 08:00 до 18:00.");
        }
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Неможливо записатися на минулий час.");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Користувача з email " + email + " не знайдено"));

        String fullName = formatFullName(user.getFullName());

        List<ClientRequest> userRequests = repository.findByUserAndStatusNot(user, "Завершено");
        for (ClientRequest req : userRequests) {
            if (req.getAppointmentTime() != null && overlaps(req.getAppointmentTime(), appointmentTime)) {
                throw new RuntimeException("Ви вже записані на іншу послугу в цей час або перетинаєтесь по часу");
            }
        }

        List<ClientRequest> sameServiceRequests = repository.findAllByServiceTypeAndStatusNotOrderByIdAsc(serviceType, "Завершено");
        for (ClientRequest req : sameServiceRequests) {
            if (req.getAppointmentTime() != null && overlaps(req.getAppointmentTime(), appointmentTime)) {
                throw new RuntimeException("Ця послуга вже зайнята у вказаний час");
            }
        }

        ClientRequest request = ClientRequest.builder()
                .username(email)
                .serviceType(serviceType)
                .status("У черзі")
                .createdAt(LocalDateTime.now())
                .appointmentTime(appointmentTime)
                .user(user)
                .userFullName(fullName)
                .build();

        repository.save(request);
    }


    private boolean overlaps(LocalDateTime existingStart, LocalDateTime newStart) {
        LocalDateTime existingEnd = existingStart.plusMinutes(30);
        return !newStart.isBefore(existingStart) && newStart.isBefore(existingEnd);
    }

    private String formatFullName(String fullName) {
        String[] parts = fullName.trim().split(" ");
        if (parts.length >= 3) {
            return String.format("%s %s. %s.", parts[0], parts[1].charAt(0), parts[2].charAt(0));
        } else {
            return fullName;
        }
    }

    public void updateStatus(Long id, String status) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus(status);
            repository.save(request);
        });
    }

    @Scheduled(fixedRate = 10000)
    public void autoProcessQueue() {
        List<ClientRequest> allRequests = repository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (ClientRequest request : allRequests) {
            if ("У черзі".equals(request.getStatus())) {
                if (request.getAppointmentTime() != null && !now.isBefore(request.getAppointmentTime())) {
                    request.setStatus("Обслуговується");
                    repository.save(request);
                }
            } else if ("Обслуговується".equals(request.getStatus())) {
                if (request.getAppointmentTime() != null &&
                        Duration.between(request.getAppointmentTime(), now).toMinutes() >= 1) {
                    request.setStatus("Завершено");
                    repository.save(request);
                }
            }
        }
    }


    public List<ClientRequest> getRequestsByServiceType(String serviceType) {
        return repository.findAllByServiceTypeOrderByIdAsc(serviceType).stream()
                .filter(r -> !"Завершено".equals(r.getStatus()))
                .toList();
    }

    public Map<String, List<ClientRequest>> getActiveRequestsByServiceType() {
        List<String> services = List.of(
                "Грошові вклади з врахуванням різних валют",
                "Кредити",
                "Банківські/кредитні картки",
                "Іпотека",
                "Індивідуальні сейфи",
                "Платежі",
                "Прийом та видача готівки/пенсія",
                "Грошові перекази",
                "Валютно-обмінні операції"
        );

        Map<String, List<ClientRequest>> requestsByService = new LinkedHashMap<>();

        for (String service : services) {
            List<ClientRequest> list = repository.findAllByServiceTypeAndStatusNotOrderByAppointmentTimeAsc(service, "Завершено");
            requestsByService.put(service, list);
        }

        return requestsByService;
    }

    public List<ClientRequest> getActiveRequestsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Користувача з email " + email + " не знайдено"));
        return repository.findByUserAndStatusNot(user, "Завершено");
    }

    public List<ClientRequest> getRequestsByUser(boolean onlyActive) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Користувача з email " + email + " не знайдено"));

        if (onlyActive) {
            return repository.findByUserAndStatusNot(user, "Завершено");
        } else {
            return repository.findAllByUsernameOrderByIdAsc(user.getEmail());
        }
    }
}