package com.nationalid.endpoint.model.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.engine.internal.Collections;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.nationalid.endpoint.enums.Genders;
import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.ValidID;

import lombok.Data;
import lombok.ToString;

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
    private Genders gender;

    @Column(name = "birthdate")
    @Nullable
    private Date birthdate;

    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ValidationError> validationErrors;

    public NationalIDrecord() {
    };

    public NationalIDrecord(ValidID validID) {
        id = String.valueOf(validID.getID());
        gender = validID.getMale() ? Genders.MALE : Genders.FEMALE;
        birthdate = validID.getBirthDate();
        validationErrors = new ArrayList<>();
    }

    public NationalIDrecord(InvalidID invalidID) {
        id = String.valueOf(invalidID.getID());
        ArrayList<ValidationError> validationErrorList = new ArrayList<>();
        invalidID.getProblems().stream().parallel().filter(Problem -> Problem != null)
                .forEach(Problem -> validationErrorList.add(new ValidationError(this, Problem)));

        validationErrors = validationErrorList;
    }

    public NationalIDrecord(String id, String... problems) {
        this.id = id;
        ArrayList<ValidationError> validationErrorList = new ArrayList<>();
        Arrays.stream(problems).parallel()
                .forEach(Problem -> validationErrorList.add(new ValidationError(this, Problem)));
        validationErrors = validationErrorList;
    }

    public Boolean HasErrors() {
        return validationErrors.size() > 0;
    }

    public List<String> fetchErrorList() {
        List<String> ErrorList = new ArrayList<>();

        validationErrors.forEach(error -> ErrorList.add(error.getErrorMessage()));

        return ErrorList;
    }

}
