package com.nationalid.endpoint.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Data;
import lombok.ToString;
import lombok.Builder.Default;

@Data
@Entity
@Table(name = "validationerror")
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nationalid", nullable = false)
    @ToString.Exclude
    @JsonBackReference
    private NationalIDrecord nationalID;

    @Column(name = "errormessage")
    private String errorMessage;

    @Column(name = "errorcode")
    @Nullable
    private String errorCode = "";

    public ValidationError() {
    };

    public ValidationError(NationalIDrecord nationalIDRecord, String errorMessage) {
        this.nationalID = nationalIDRecord;
        this.errorMessage = errorMessage;
    }
}
