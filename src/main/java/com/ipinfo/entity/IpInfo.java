package com.ipinfo.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "ip_info")
public class IpInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", unique = true, nullable = false, length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String payload;
}