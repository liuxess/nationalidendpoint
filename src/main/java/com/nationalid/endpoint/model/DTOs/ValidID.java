package com.nationalid.endpoint.model.DTOs;

import java.time.LocalDate;
import java.util.Optional;

import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nationalid.SegmentedNationalID;
import nationalid.enums.Gender;
import nationalid.enums.NationalIDSegmentType;
import nationalid.models.Segments.Specific.BirthDateSegment;
import nationalid.models.Segments.Specific.GenderSegment;

/**
 * Represents an ID that was validated and has errors
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidID extends ValidatedIDBase {

    Gender gender;
    LocalDate birthDate;

    /**
     * Parses out needed values out of a valid Segmented National ID
     * 
     * @param nationalID to parse out
     */
    public ValidID(SegmentedNationalID nationalID) {
        super(nationalID);

        // If this an actually Valid ID, all Optionals should be filled either way
        GenderSegment genderSegment = (GenderSegment) nationalID.getSegment(NationalIDSegmentType.GENDER).get();
        gender = genderSegment.getGender().get();
        BirthDateSegment birthDateSegment = (BirthDateSegment) nationalID.getSegment(NationalIDSegmentType.BIRTH_DATE)
                .get();

        Optional<Integer> centuryOfBirth = genderSegment.getCentury();
        birthDate = birthDateSegment.toDate(centuryOfBirth.get()).get();
    }

    /**
     * Parses out needed values out of National ID
     * 
     * @param record to parse
     */
    public ValidID(NationalIDrecord record) {
        super(record);
        this.gender = record.getGender();
        this.birthDate = record.getBirthdate();
    }

    /**
     * Parses out needed values out of National ID without errors
     * 
     * @param record to parse
     */
    public ValidID(NationalIDDTO DTO) {
        this(DTO.getNationalIDrecord());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nationalid.endpoint.model.responseObjects.ValidatedIDBase#IsValid()
     */
    @Override
    public boolean IsValid() {
        return true;
    }

}
