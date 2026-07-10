package com.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
public class Driver {

    @Id
    @Column(name = "driver_id")
    private String driverId;

    @Column(nullable = false)
    private String name;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    public Driver(String driverId, String name, String phone, String email, Region region) {
        this.driverId = driverId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.region = region;
    }
}
