package com.ipinfo.repository;

import com.ipinfo.entity.IpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IpInfoRepository extends JpaRepository<IpInfo, Long> {
    Optional<IpInfo> findByIpAddress(String ipAddress);
}