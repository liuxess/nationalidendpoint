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

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "validationerror")
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "id")
    private long id;

    // @ManyToOne(fetch = FetchType.EAGER, optional = false)
    // @JoinColumn(name = "nationalid", nullable = false)
    // @ToString.Exclude
    // @JsonBackReference
    @Column(name = "nationalID")
    private String nationalID;

    @Column(name = "errormessage")
    private String errorMessage;

    @Column(name = "errorcode")
    @Nullable
    private String errorCode = "";

    public ValidationError() {
    };

    public ValidationError(String nationalID, String errorMessage) {
        this.nationalID = nationalID;
        this.errorMessage = errorMessage;
    }
}
