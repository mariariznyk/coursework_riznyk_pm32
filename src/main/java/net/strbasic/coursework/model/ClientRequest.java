package net.strbasic.coursework.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String serviceType;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String userFullName;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;
}
