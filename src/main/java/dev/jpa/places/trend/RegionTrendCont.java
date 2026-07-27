package dev.jpa.places.trend;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionTrendCont {

    private final RegionTrendRepository regionTrendRepository;

    public RegionTrendCont(RegionTrendRepository regionTrendRepository) {
        this.regionTrendRepository = regionTrendRepository;
    }

    @GetMapping("/trending")
    public List<TrendCardView> trending(@RequestParam(defaultValue = "3") int limit) {
        return regionTrendRepository.findLatestTopN(limit);
    }
}
