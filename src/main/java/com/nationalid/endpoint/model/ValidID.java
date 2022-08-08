package com.nationalid.endpoint.model;

import java.time.LocalDate;
import java.util.Optional;

import com.nationalid.endpoint.model.entity.NationalIDrecord;

import lombok.Data;
import nationalid.SegmentedNationalID;
import nationalid.enums.Gender;
import nationalid.enums.NationalIDSegmentType;
import nationalid.models.Segments.Specific.BirthDateSegment;
import nationalid.models.Segments.Specific.GenderSegment;

@Data
public class ValidID {

    String ID;
    Gender gender;
    LocalDate birthDate;

    public ValidID(SegmentedNationalID nationalID) {
        this.ID = nationalID.getNationalID().getID();

        // If this an actually Valid ID, all Optionals should be filled either way
        GenderSegment genderSegment = (GenderSegment) nationalID.getSegment(NationalIDSegmentType.GENDER).get();
        gender = genderSegment.getGender().get();
        BirthDateSegment birthDateSegment = (BirthDateSegment) nationalID.getSegment(NationalIDSegmentType.BIRTH_DATE)
                .get();

        Optional<Integer> centuryOfBirth = genderSegment.getCentury();
        birthDate = birthDateSegment.toDate(centuryOfBirth.get()).get();
    }

    public ValidID(NationalIDrecord record) {
        this.ID = record.getId();
        this.gender = record.getGender();
        this.birthDate = record.getBirthdate();
    }

    public ValidID(NationalIDWithErrors nationalIDWithErrors) {
        this(nationalIDWithErrors.getNationalIDrecord());
    }

}
