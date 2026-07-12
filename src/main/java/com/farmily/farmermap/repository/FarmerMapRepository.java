package com.farmily.farmermap.repository;

import com.farmily.user.model.Farmer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmerMapRepository extends JpaRepository<Farmer, Integer> {

    @EntityGraph(attributePaths = "cityDistrict")
    List<Farmer> findByFarmerStatusOrderByFarmerIdAsc(Farmer.FarmerStatus farmerStatus);

    @EntityGraph(attributePaths = "cityDistrict")
    Optional<Farmer> findByFarmerIdAndFarmerStatus(Integer farmerId, Farmer.FarmerStatus farmerStatus);
}
