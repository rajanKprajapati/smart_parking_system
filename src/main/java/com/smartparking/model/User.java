package com.smartparking.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model representing a registered user in the Smart Parking system.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role {
        CUSTOMER, PROVIDER, ADMIN
    }

    public enum AccountStatus {
        ACTIVE, SUSPENDED, INACTIVE
    }

    private Long userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private Role role;
    private AccountStatus accountStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
        this.role = Role.CUSTOMER;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    public User(Long userId, String fullName, String email, String passwordHash, String phoneNumber, Role role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : Role.CUSTOMER;
        this.accountStatus = AccountStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRole(String roleStr) {
        if (roleStr != null) {
            try {
                this.role = Role.valueOf(roleStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.role = Role.CUSTOMER;
            }
        }
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public void setAccountStatus(String statusStr) {
        if (statusStr != null) {
            try {
                this.accountStatus = AccountStatus.valueOf(statusStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                this.accountStatus = AccountStatus.ACTIVE;
            }
        }
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isCustomer() {
        return Role.CUSTOMER.equals(this.role);
    }

    public boolean isProvider() {
        return Role.PROVIDER.equals(this.role);
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.accountStatus);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", accountStatus=" + accountStatus +
                '}';
    }
}
