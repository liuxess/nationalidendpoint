package com.nationalid.endpoint.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nationalid.endpoint.model.entity.ValidationError;

import java.util.List;

@Repository
public interface ValidationErrorRepository extends JpaRepository<ValidationError, Long> {
        @Query("FROM ValidationError VE WHERE " +
                        "(VE.nationalID = :nationalid)")
        List<ValidationError> findAllByNationalID(@Param("nationalid") String nationalid);

        @Query(" FROM ValidationError VE WHERE " +
                        "(VE.nationalID IN (:nationalids))")
        List<ValidationError> findAllByNationalIDs(@Param("nationalids") List<String> nationalids);

        @Query("SELECT DISTINCT VE.nationalID FROM ValidationError VE ")
        List<String> fetchUniqueNationalIDs();

}
