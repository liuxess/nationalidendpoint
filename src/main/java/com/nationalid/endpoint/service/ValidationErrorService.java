package com.nationalid.endpoint.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nationalid.endpoint.repositories.ValidationErrorRepository;

import lombok.RequiredArgsConstructor;

import com.nationalid.endpoint.model.entity.ValidationError;

/**
 * Service for Validation Error records in the DB
 */
@Service
@RequiredArgsConstructor
public class ValidationErrorService {

    private final ValidationErrorRepository validationErrorRepository;

    /**
     * Gets all ValidationErrors based on the provided national ID
     * 
     * @param nationalID to fetch errors for
     * @return Validation errors with the national ID
     */
    public List<ValidationError> getValidationErrorsForID(String nationalID) {
        return validationErrorRepository.findAllByNationalID(nationalID);
    }

    /**
     * Gets all Validation Errors based on the provided national IDs
     * 
     * @param IDs to fetch errors for
     * @return Validation errors from one of the IDs
     */
    public List<ValidationError> getValidationErrorsForIDs(List<String> IDs) {
        return validationErrorRepository.findAllByNationalIDs(IDs);
    }

    /**
     * Save all passed Validation errors
     * 
     * @param validationErrors to save
     */
    public void saveValidationErrors(List<ValidationError> validationErrors) {
        validationErrorRepository.saveAll(validationErrors);
    }

    /**
     * Fetches a list of all national IDs that have a validation Error registered
     * 
     * @return list of IDs
     */
    public List<String> fetchUniqueNationalIDs() {
        return validationErrorRepository.fetchUniqueNationalIDs();
    }

}
