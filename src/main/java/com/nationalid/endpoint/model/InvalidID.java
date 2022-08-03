package com.nationalid.endpoint.model;

import java.util.List;

import com.nationalid.endpoint.model.entity.NationalIDrecord;

import lombok.Data;
import nationalid.SegmentedNationalID;
import nationalid.models.NationalID;

@Data
public class InvalidID {

    private List<String> Problems;

    private String ID;

    public InvalidID(SegmentedNationalID segmentedID) {
        Problems = segmentedID.getProblemList();
        this.ID = String.valueOf(segmentedID.getID().getID());
    }

    public InvalidID(NationalIDrecord record) {
        this.ID = record.getId();
        Problems = record.fetchErrorList();
    }

    public String getID() {
        return ID;
    }

}
