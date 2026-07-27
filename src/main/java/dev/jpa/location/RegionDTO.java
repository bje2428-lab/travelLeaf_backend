package dev.jpa.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegionDTO {

    private Long regionId;
    private String regionName;

    public static RegionDTO fromEntity(Region region) {
        return new RegionDTO(
                region.getRegionId(),
                region.getRegionName()
        );
    }
}
