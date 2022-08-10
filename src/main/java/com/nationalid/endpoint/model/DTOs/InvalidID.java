package com.nationalid.endpoint.model.DTOs;

import java.util.List;

import com.nationalid.endpoint.model.entity.ValidationError;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nationalid.SegmentedNationalID;

/**
 * Represents an ID that was validated and has errors
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InvalidID extends ValidatedIDBase {

    private List<String> Problems;

    /**
     * @param segmentedID to be treated as Invalid
     */
    public InvalidID(SegmentedNationalID segmentedID) {
        super(segmentedID);
        Problems = segmentedID.getProblemList();
    }

    /**
     * @param DTO to be treated as Invalid
     */
    public InvalidID(NationalIDDTO DTO) {
        super(DTO);
        Problems = DTO.getErrors().stream().map(error -> error.getErrorMessage()).toList();
    }

    /**
     * Will turn all the problems from this Invalid ID and turn them to a list of
     * validation errors
     * 
     * @return list of validation errors
     */
    public List<ValidationError> toValidationErrors() {
        return Problems.stream().parallel().map(problem -> new ValidationError(getID(), problem)).toList();
    }

    /**
     * To verify whether or not the current instance is valid or not
     */
    @Override
    public boolean IsValid() {
        return false;
    }

}
