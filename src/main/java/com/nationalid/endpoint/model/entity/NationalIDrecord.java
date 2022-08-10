package com.nationalid.endpoint.model.entity;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.lang.Nullable;

import com.nationalid.endpoint.model.DTOs.InvalidID;
import com.nationalid.endpoint.model.DTOs.ValidID;

import lombok.Data;
import nationalid.enums.Gender;

/**
 * Represents data from the nationalID table
 */
@Data
@Entity
@Table(name = "nationalID")
public class NationalIDrecord {

    @Id
    @Column(name = "id")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    @Nullable
    private Gender gender;

    @Column(name = "birthdate")
    @Nullable
    private LocalDate birthdate;

    public NationalIDrecord() {
    };

    /**
     * parses out the metadata from a validated valid ID
     * 
     * @param validID to parse out
     * @see com.nationalid.endpoint.model.DTOs.ValidID
     */
    public NationalIDrecord(ValidID validID) {
        id = String.valueOf(validID.getID());
        gender = validID.getGender();
        birthdate = validID.getBirthDate();
    }

    /**
     * parses out the metadata from a validated invalid ID
     * 
     * @param invalidID to parse out
     * @see com.nationalid.endpoint.model.DTOs.InvalidID
     */
    public NationalIDrecord(InvalidID invalidID) {
        id = String.valueOf(invalidID.getID());
    }

}
