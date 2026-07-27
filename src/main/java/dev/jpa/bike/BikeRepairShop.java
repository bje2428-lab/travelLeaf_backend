package dev.jpa.bike;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bike_repair_shop")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // <- 여기에 toBuilder=true 추가
public class BikeRepairShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repairId;

    private String name;
    private String address;

    private Double lat;
    private Double lng;

    private String phone;
    private String openTime;

    @Column(name = "is_onsite_service")
    private Boolean isOnsiteService;

    @Enumerated(EnumType.STRING)
    private BikeDataSource source;
}
