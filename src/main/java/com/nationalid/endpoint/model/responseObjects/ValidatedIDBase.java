package com.nationalid.endpoint.model.responseObjects;

import com.nationalid.endpoint.model.NationalIDWithErrors;
import com.nationalid.endpoint.model.entity.NationalIDrecord;

import lombok.Data;
import nationalid.SegmentedNationalID;

@Data
public abstract class ValidatedIDBase {

    private String ID;

    protected ValidatedIDBase(String ID) {
        this.ID = ID;
    }

    protected ValidatedIDBase(NationalIDrecord record) {
        this(record.getId());
    }

    protected ValidatedIDBase(SegmentedNationalID segmentedID) {
        this(segmentedID.getNationalID().getID());
    }

    protected ValidatedIDBase(NationalIDWithErrors nationalIDWithErrors) {
        this(nationalIDWithErrors.getNationalIDrecord());
    }

    public abstract boolean IsValid();

}
