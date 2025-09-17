package com.freightops.billetrerie.repository;

import com.freightops.billetrerie.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByPassengerName(String passengerName);

    List<Ticket> findByOriginAndDestination(String origin, String destination);

    List<Ticket> findByTravelDate(LocalDate travelDate);

    List<Ticket> findByStatus(String status);

    @Query("SELECT t FROM Ticket t WHERE t.travelDate BETWEEN :startDate AND :endDate")
    List<Ticket> findByTravelDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.travelDate = :date")
    Double getTotalRevenueByDate(@Param("date") LocalDate date);
}
