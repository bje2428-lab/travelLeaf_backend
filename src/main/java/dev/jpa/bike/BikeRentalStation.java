package dev.jpa.bike;

import javax.sql.DataSource;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bike_rental_station")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // <- 여기에 toBuilder=true 추가
public class BikeRentalStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rentalId;

    private String name;
    private String address;

    private Double lat;
    private Double lng;

    private String openTime;
    private Integer bikeCount;
    
    private String phone; // 대여소 전화번호
    
    @Enumerated(EnumType.STRING)
    private BikeDataSource source;

}
