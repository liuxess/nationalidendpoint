package com.nationalid.endpoint.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.entity.ValidationError;

import java.util.List;

@Repository
public interface ValidationErrorRepository extends JpaRepository<ValidationError, Long> {
        @Query("FROM ValidationError ValidationErrors WHERE " +
                        "(ValidationErrors.nationalID = :nationalid)")
        List<ValidationError> findAllByNationalID(@Param("nationalid") NationalIDrecord nationalid);

        @Query(" FROM ValidationError ValidationErrors WHERE " +
                        "(ValidationErrors.nationalID.id IN (:nationalids))")
        List<ValidationError> findAllByNationalIDs(@Param("nationalids") List<String> nationalids);

}
