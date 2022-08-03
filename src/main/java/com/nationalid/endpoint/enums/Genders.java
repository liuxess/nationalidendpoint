package com.nationalid.endpoint.enums;

import lombok.ToString;

public enum Genders {
    MALE("Male"), FEMALE("Male");

    private String genderAsString;

    Genders(String gender) {
        this.genderAsString = gender;
    }

    public String toString() {
        return genderAsString;
    }

}
