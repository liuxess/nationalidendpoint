package com.nationalid.endpoint.model;

import java.util.ArrayList;
import java.util.List;

import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.entity.ValidationError;
import com.nationalid.endpoint.model.responseObjects.InvalidID;
import com.nationalid.endpoint.model.responseObjects.ValidID;

import lombok.Data;
import nationalid.SegmentedNationalID;

@Data
public class NationalIDWithErrors {
    private NationalIDrecord nationalIDrecord;
    private List<ValidationError> errors;

    public NationalIDWithErrors(NationalIDrecord nationalIDrecord) {
        this.nationalIDrecord = nationalIDrecord;
        this.errors = new ArrayList<>();
    }

    public NationalIDWithErrors(NationalIDrecord nationalIDrecord, List<ValidationError> errors) {
        this.nationalIDrecord = nationalIDrecord;
        this.errors = errors;
    }

    public NationalIDWithErrors(InvalidID invalidID) {
        this.nationalIDrecord = new NationalIDrecord(invalidID);
        this.errors = invalidID.toValidationErrors();
    }

    public NationalIDWithErrors(ValidID validID) {
        this.nationalIDrecord = new NationalIDrecord(validID);
        this.errors = new ArrayList<>();
    }

    public boolean HasErrors() {
        if (errors != null)
            return !errors.isEmpty();

        return false;
    }

}
