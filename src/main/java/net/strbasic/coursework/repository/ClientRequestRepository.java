package net.strbasic.coursework.repository;

import net.strbasic.coursework.model.ClientRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import net.strbasic.coursework.model.User;

import java.util.List;

public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {

    List<ClientRequest> findAllByUsernameOrderByIdAsc(String username); // ← додай цей метод

    List<ClientRequest> findAllByStatusOrderByIdAsc(String status);

    List<ClientRequest> findAllByServiceTypeOrderByIdAsc(String serviceType);

    List<ClientRequest> findAllByServiceTypeAndStatusNotOrderByIdAsc(String serviceType, String status);

    //List<ClientRequest> findByUserId(Long userId);
    List<ClientRequest> findByUserAndStatusNot(User user, String status);

    List<ClientRequest> findAllByServiceTypeAndStatusNotOrderByAppointmentTimeAsc(String serviceType, String excludedStatus);



}
