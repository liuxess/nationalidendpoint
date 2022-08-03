package com.nationalid.endpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nationalid.endpoint.model.entity.NationalIDrecord;

@Repository
public interface NationalIDRepository extends JpaRepository<NationalIDrecord, String> {
    @Query("FROM NationalIDrecord NationalIDs WHERE " +
            "(NationalIDs.id IN (:IDs))")
    List<NationalIDrecord> findAllExistingFromList(List<String> IDs);
}