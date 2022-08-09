package com.nationalid.endpoint.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nationalid.endpoint.repositories.ValidationErrorRepository;

import lombok.RequiredArgsConstructor;

import com.nationalid.endpoint.model.entity.ValidationError;

@Service
@RequiredArgsConstructor
public class ValidationErrorService {

    private final ValidationErrorRepository validationErrorRepository;

    public List<ValidationError> getValidationErrorsForID(String ID) {
        return validationErrorRepository.findAllByNationalID(ID);
    }

    public List<ValidationError> getValidationErrorsForIDs(List<String> IDs) {
        return validationErrorRepository.findAllByNationalIDs(IDs);
    }

    public void saveValidationErrors(List<ValidationError> validationErrors) {
        validationErrorRepository.saveAll(validationErrors);
    }

    public List<String> fetchUniqueNationalIDs() {
        return validationErrorRepository.fetchUniqueNationalIDs();
    }

}
