package dev.jpa.bike;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BikeAdminController {

    private final BikeService bikeService;

    // ===============================
    // 수리점 (검색 + 페이징 + CRUD)
    // ===============================

    // ✅ 목록 조회 (검색 + 페이징)
    @GetMapping("/repairs")
    public Page<BikeRepairShop> getRepairs(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("repairId").descending());

        return bikeService.searchRepairShops(keyword, pageable);
    }

    // ✅ 추가
    @PostMapping("/repairs")
    public BikeRepairShop addRepairShop(
            @RequestBody BikeRepairShop shop
    ) {
        return bikeService.addRepairShop(shop);
    }

    // ✅ 수정
    @PutMapping("/repairs/{id}")
    public BikeRepairShop updateRepairShop(
            @PathVariable("id") Long id,
            @RequestBody BikeRepairShop updatedShop
    ) {
        return bikeService.updateRepairShop(id, updatedShop);
    }

    // ✅ 삭제
    @DeleteMapping("/repairs/{id}")
    public ResponseEntity<Void> deleteRepairShop(
            @PathVariable("id") Long id
    ) {
        bikeService.deleteRepairShop(id);
        return ResponseEntity.noContent().build();
    }

    // ===============================
    // 대여소 (검색 + 페이징 + CRUD)
    // ===============================

    // ✅ 목록 조회 (검색 + 페이징)
    @GetMapping("/rentals")
    public Page<BikeRentalStation> getRentals(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("rentalId").descending());

        return bikeService.searchRentalStations(keyword, pageable);
    }

    // ✅ 추가
    @PostMapping("/rentals")
    public BikeRentalStation addRentalStation(
            @RequestBody BikeRentalStation station
    ) {
        return bikeService.addRentalStation(station);
    }

    // ✅ 수정
    @PutMapping("/rentals/{id}")
    public BikeRentalStation updateRentalStation(
            @PathVariable("id") Long id,
            @RequestBody BikeRentalStation updatedStation
    ) {
        return bikeService.updateRentalStation(id, updatedStation);
    }

    // ✅ 삭제
    @DeleteMapping("/rentals/{id}")
    public ResponseEntity<Void> deleteRentalStation(
            @PathVariable("id") Long id
    ) {
        bikeService.deleteRentalStation(id);
        return ResponseEntity.noContent().build();
    }
}
