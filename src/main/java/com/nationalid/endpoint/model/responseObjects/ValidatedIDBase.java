package com.nationalid.endpoint.model.responseObjects;

import com.nationalid.endpoint.model.DTOs.NationalIDDTO;
import com.nationalid.endpoint.model.entity.NationalIDrecord;

import lombok.Data;
import nationalid.SegmentedNationalID;

/**
 * Base class for validated IDs
 */
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

    protected ValidatedIDBase(NationalIDDTO nationalIDDTO) {
        this(nationalIDDTO.getNationalIDrecord());
    }

    public abstract boolean IsValid();

}
