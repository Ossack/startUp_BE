package com.sparta.startup_be.coordinate;

import com.sparta.startup_be.model.CoordinateEstate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoordinateRepository extends JpaRepository<CoordinateEstate, Long> {
    List<CoordinateEstate> findAllByXBetweenAndYBetween(Float minX, Float maxX, Float minY, Float maxY );
    List<CoordinateEstate> findAllByXBetween(Float minX, Float maxX );

    CoordinateEstate findByEstateid(Long estateid);
}
