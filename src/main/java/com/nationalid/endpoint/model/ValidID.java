package com.nationalid.endpoint.model;

import java.util.Date;

import lombok.Data;
import nationalid.SegmentedNationalID;
import nationalid.enums.NationalIDSegmentType;
import nationalid.models.NationalID;
import nationalid.models.Segments.Specific.BirthDateSegment;
import nationalid.models.Segments.Specific.GenderSegment;

@Data
public class ValidID {

    NationalID ID;
    Boolean male;
    Date birthDate;

    public ValidID(SegmentedNationalID nationalID) {
        this.ID = nationalID.getID();
        GenderSegment genderSegment = (GenderSegment) nationalID.getSegment(NationalIDSegmentType.GENDER);
        male = genderSegment.IsMale();
        BirthDateSegment birthDateSegment = (BirthDateSegment) nationalID.getSegment(NationalIDSegmentType.BIRTH_DATE);
        try {
            birthDate = birthDateSegment.toDate(genderSegment.getCentury());
        } catch (Exception ex) {
            // TODO: do something probably, not sure
        }
    }

    public long getID() {
        return ID.getID();
    }
}
