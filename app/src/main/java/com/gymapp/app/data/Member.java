package com.gymapp.app.data;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    // Profile details
    private String firstName;
    private String lastName;
    private String gender; // M/F/Other
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String address;

    private Double weightKg;
    private Double heightCm;

    @Column(unique = true)
    private String qrCode;

    // Medical history
    private boolean hypertension;
    private boolean hypotension;
    private boolean asthma;
    private boolean appendicitis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.INACTIVE;

    private LocalDate subscriptionEndAt;

    public enum SubscriptionStatus { ACTIVE, INACTIVE }

    public Member() { }

    public Member(String fullName) {
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public boolean isHypertension() { return hypertension; }
    public void setHypertension(boolean hypertension) { this.hypertension = hypertension; }
    public boolean isHypotension() { return hypotension; }
    public void setHypotension(boolean hypotension) { this.hypotension = hypotension; }
    public boolean isAsthma() { return asthma; }
    public void setAsthma(boolean asthma) { this.asthma = asthma; }
    public boolean isAppendicitis() { return appendicitis; }
    public void setAppendicitis(boolean appendicitis) { this.appendicitis = appendicitis; }
    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
    public LocalDate getSubscriptionEndAt() { return subscriptionEndAt; }
    public void setSubscriptionEndAt(LocalDate subscriptionEndAt) { this.subscriptionEndAt = subscriptionEndAt; }
}
