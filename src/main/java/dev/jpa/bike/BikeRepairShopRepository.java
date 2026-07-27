package dev.jpa.bike;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BikeRepairShopRepository
        extends JpaRepository<BikeRepairShop, Long> {

    @Query(value = """
        SELECT *
        FROM bike_repair_shop
        WHERE (
            6371 * ACOS(
                COS(:lat * ACOS(-1) / 180)
                * COS(lat * ACOS(-1) / 180)
                * COS((lng - :lng) * ACOS(-1) / 180)
                + SIN(:lat * ACOS(-1) / 180)
                * SIN(lat * ACOS(-1) / 180)
            )
        ) <= :radius
        ORDER BY lat
        """, nativeQuery = true)
    List<BikeRepairShop> findNearby(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radius
    );
    @Query(value = """
        SELECT *
        FROM bike_repair_shop
        WHERE lat BETWEEN :swLat AND :neLat
          AND lng BETWEEN :swLng AND :neLng
        """, nativeQuery = true)
    List<BikeRepairShop> findWithinBounds(
        @Param("swLat") double swLat,
        @Param("neLat") double neLat,
        @Param("swLng") double swLng,
        @Param("neLng") double neLng
    );
    Page<BikeRepairShop> findByNameContainingOrAddressContainingOrPhoneContaining(
        String name,
        String address,
        String phone,
        Pageable pageable
);
    
    boolean existsByNameAndAddress(String name, String address);
}
