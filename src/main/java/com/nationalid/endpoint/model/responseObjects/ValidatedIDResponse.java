package com.nationalid.endpoint.model.responseObjects;

import java.util.ArrayList;
import java.util.List;

import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.ValidID;

import lombok.Getter;
import nationalid.verificationapp.CategorizedIDLists;

@Getter
public class ValidatedIDResponse {

    int total;
    int numberOfValidIDs;
    final List<ValidID> validIDs;
    final List<InvalidID> invalidIDs;

    public ValidatedIDResponse(CategorizedIDLists categorizedIDLists) {
        validIDs = new ArrayList<>();
        invalidIDs = new ArrayList<>();

        categorizedIDLists.getCorrect().stream().parallel()
                .forEach(correctID -> validIDs.add(new ValidID(correctID)));

        categorizedIDLists.getIncorrect().stream().parallel()
                .forEach(incorrectID -> {
                    if (incorrectID != null)
                        invalidIDs.add(new InvalidID(incorrectID));
                });

        numberOfValidIDs = validIDs.size();
        total = numberOfValidIDs + invalidIDs.size();
    }

    public Boolean AnyInvalid() {
        return !invalidIDs.isEmpty();
    }

}
