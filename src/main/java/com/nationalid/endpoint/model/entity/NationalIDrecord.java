package com.nationalid.endpoint.model.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.ValidID;

import lombok.Data;
import nationalid.enums.Gender;

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

    public NationalIDrecord(ValidID validID) {
        id = String.valueOf(validID.getID());
        gender = validID.getGender();
        birthdate = validID.getBirthDate();
    }

    public NationalIDrecord(InvalidID invalidID) {
        id = String.valueOf(invalidID.getID());
        ArrayList<ValidationError> validationErrorList = new ArrayList<>();
        invalidID.getProblems().stream().parallel().filter(Problem -> Problem != null)
                .forEach(Problem -> validationErrorList.add(new ValidationError(id, Problem)));
    }

}
