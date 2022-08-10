package com.nationalid.endpoint.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * represent a single validation problem stored in table
 */
@Data
@Entity
@Table(name = "validationerror")
public class ValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "id")
    private long id;

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
