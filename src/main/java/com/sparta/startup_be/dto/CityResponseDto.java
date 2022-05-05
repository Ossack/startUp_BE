package com.sparta.startup_be.dto;

import com.sparta.startup_be.model.Coordinate;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CityResponseDto {
    private String title;
    private CoordinateResponseDto coordinate;
    private List<EstateResponseDto> estate;

    public CityResponseDto(String title, CoordinateResponseDto coordinate, List<EstateResponseDto> estate){
        this.title = title;
        this.coordinate = coordinate;
        this.estate = estate;
    }
}
