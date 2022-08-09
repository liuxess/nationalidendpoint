package com.nationalid.endpoint.model.responseObjects;

import java.util.List;

import com.nationalid.endpoint.model.NationalIDWithErrors;
import com.nationalid.endpoint.model.entity.ValidationError;

import lombok.Data;
import nationalid.SegmentedNationalID;

@Data
public class InvalidID extends ValidatedIDBase {

    private List<String> Problems;

    public InvalidID(SegmentedNationalID segmentedID) {
        super(segmentedID);
        Problems = segmentedID.getProblemList();
    }

    public InvalidID(NationalIDWithErrors nationalIDWithErrors) {
        super(nationalIDWithErrors);
        Problems = nationalIDWithErrors.getErrors().stream().map(error -> error.getErrorMessage()).toList();
    }

    public List<ValidationError> toValidationErrors() {
        return Problems.stream().parallel().map(problem -> new ValidationError(getID(), problem)).toList();
    }

    @Override
    public boolean IsValid() {
        return false;
    }

}
