package com.nationalid.endpoint.model;

import java.util.List;

import lombok.Data;
import nationalid.SegmentedNationalID;
import nationalid.models.NationalID;

@Data
public class InvalidID {

    private List<String> Problems;

    private NationalID ID;

    public InvalidID(SegmentedNationalID segmentedID) {
        Problems = segmentedID.getProblemList();
        this.ID = segmentedID.getID();
    }

    public long getID() {
        return ID.getID();
    }

}
