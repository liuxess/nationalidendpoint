package com.nationalid.endpoint.model.DTOs;

import java.util.ArrayList;
import java.util.List;

import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.entity.ValidationError;

import lombok.Data;

/**
 * Acts as a DTO to couple national IDs and their validation errors
 * 
 * @see com.nationalid.endpoint.model.entity.NationalIDrecord
 * @see com.nationalid.endpoint.model.entity.ValidationError
 */
@Data
public class NationalIDDTO {
    private NationalIDrecord nationalIDrecord;
    private List<ValidationError> errors;

    /**
     * Binds without errors
     * 
     * @param nationalIDrecord
     */
    public NationalIDDTO(NationalIDrecord nationalIDrecord) {
        this.nationalIDrecord = nationalIDrecord;
        this.errors = new ArrayList<>();
    }

    /**
     * Couples NationalID with the errors
     * 
     * @param nationalIDrecord
     * @param errors
     */
    public NationalIDDTO(NationalIDrecord nationalIDrecord, List<ValidationError> errors) {
        this.nationalIDrecord = nationalIDrecord;
        this.errors = errors;
    }

    /**
     * parses out NationalID and it's errors out of the InvalidID
     * 
     * @param invalidID to parse
     */
    public NationalIDDTO(InvalidID invalidID) {
        this.nationalIDrecord = new NationalIDrecord(invalidID);
        this.errors = invalidID.getProblems().stream().parallel()
                .map(problem -> new ValidationError(invalidID.getID(), problem)).toList();
    }

    /**
     * parses out NationalID details with an empty error list out of a Valid ID
     * 
     * @param validID to parse
     */
    public NationalIDDTO(ValidID validID) {
        this.nationalIDrecord = new NationalIDrecord(validID);
        this.errors = new ArrayList<>();
    }

    /**
     * @return whether the current NationalID has errors
     */
    public boolean HasErrors() {
        if (errors != null)
            return !errors.isEmpty();

        return false;
    }

}
