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
public class CityDTO {

    private Long cityId;
    private String cityName;
    private Long regionId;

    public static CityDTO fromEntity(City city) {
        return new CityDTO(
                city.getCityId(),
                city.getCityName(),
                city.getRegion().getRegionId()
        );
    }
}
