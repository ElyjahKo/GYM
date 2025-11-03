package com.gymapp.app.data;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private String type; // legacy label

    // New fields
    @Column(nullable = false)
    private String plan; // Mensuel/Trimestriel/Annuel

    @Column(nullable = false)
    private Integer quantityMonths; // 1/3/6/12

    @Column(nullable = false)
    private Integer price; // FCFA

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private LocalDate startAt;

    @Column(nullable = false)
    private LocalDate endAt;

    public enum Status { ACTIVE, INACTIVE }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public Integer getQuantityMonths() { return quantityMonths; }
    public void setQuantityMonths(Integer quantityMonths) { this.quantityMonths = quantityMonths; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDate getStartAt() { return startAt; }
    public void setStartAt(LocalDate startAt) { this.startAt = startAt; }
    public LocalDate getEndAt() { return endAt; }
    public void setEndAt(LocalDate endAt) { this.endAt = endAt; }
}
