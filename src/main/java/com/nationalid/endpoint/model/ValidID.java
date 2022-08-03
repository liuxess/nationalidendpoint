package com.nationalid.endpoint.model;

import java.util.Date;

import com.nationalid.endpoint.enums.Genders;
import com.nationalid.endpoint.model.entity.NationalIDrecord;

import lombok.Data;
import nationalid.SegmentedNationalID;
import nationalid.enums.NationalIDSegmentType;
import nationalid.models.NationalID;
import nationalid.models.Segments.Specific.BirthDateSegment;
import nationalid.models.Segments.Specific.GenderSegment;

@Data
public class ValidID {

    String ID;
    Genders gender;
    Date birthDate;

    public ValidID(SegmentedNationalID nationalID) {
        this.ID = String.valueOf(nationalID.getID().getID());
        GenderSegment genderSegment = (GenderSegment) nationalID.getSegment(NationalIDSegmentType.GENDER);
        gender = genderSegment.IsMale() ? Genders.MALE : Genders.FEMALE;
        BirthDateSegment birthDateSegment = (BirthDateSegment) nationalID.getSegment(NationalIDSegmentType.BIRTH_DATE);
        try {
            birthDate = birthDateSegment.toDate(genderSegment.getCentury());
        } catch (Exception ex) {
            // TODO: do something probably, not sure
            throw new RuntimeException(ex);
        }
    }

    public ValidID(NationalIDrecord record) {
        this.ID = record.getId();
        this.gender = record.getGender();
        this.birthDate = record.getBirthdate();
    }

    public String getID() {
        return ID;
    }
}
