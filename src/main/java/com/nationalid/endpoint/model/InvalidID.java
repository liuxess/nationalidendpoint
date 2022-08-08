package com.nationalid.endpoint.model;

import java.util.List;

import com.nationalid.endpoint.model.entity.ValidationError;

import lombok.Data;
import nationalid.SegmentedNationalID;

@Data
public class InvalidID {

    private List<String> Problems;

    private String ID;

    public InvalidID(SegmentedNationalID segmentedID) {
        Problems = segmentedID.getProblemList();
        this.ID = segmentedID.getNationalID().getID();
    }

    public InvalidID(NationalIDWithErrors nationalIDWithErrors) {
        this.ID = nationalIDWithErrors.getNationalIDrecord().getId();
        Problems = nationalIDWithErrors.getErrors().stream().map(error -> error.getErrorMessage()).toList();
    }

    public String getID() {
        return ID;
    }

    public List<ValidationError> toValidationErrors() {
        return Problems.stream().parallel().map(problem -> new ValidationError(ID, problem)).toList();
    }

}
