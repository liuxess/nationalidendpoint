package com.nationalid.endpoint.model.responseObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.ValidID;
import com.nationalid.endpoint.model.entity.NationalIDrecord;

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
        RecalculateTotals();
    }

    public ValidatedIDResponse(List<ValidID> validIDs, List<InvalidID> invalidIDs) {
        this.validIDs = validIDs;
        this.invalidIDs = invalidIDs;
        RecalculateTotals();
    }

    public ValidatedIDResponse(List<NationalIDrecord> nationalIDrecords) {
        validIDs = new ArrayList<>();
        invalidIDs = new ArrayList<>();

        nationalIDrecords.stream().parallel().forEach(
                record -> {
                    if (!record.HasErrors()) {
                        validIDs.add(new ValidID(record));
                    } else {
                        invalidIDs.add(new InvalidID(record));
                    }
                });

        RecalculateTotals();
    }

    private void RecalculateTotals() {
        numberOfValidIDs = validIDs.size();
        total = numberOfValidIDs + invalidIDs.size();
    }

    public Boolean AnyInvalid() {
        return !invalidIDs.isEmpty();
    }

    public static ValidatedIDResponse mergeMultiple(ValidatedIDResponse... responses) {
        final List<ValidID> validIDs = new ArrayList<>();
        final List<InvalidID> invalidIDs = new ArrayList<>();

        Arrays.stream(responses).parallel().forEach(
                response -> {
                    validIDs.addAll(response.getValidIDs());
                    invalidIDs.addAll(response.getInvalidIDs());
                });

        return new ValidatedIDResponse(validIDs, invalidIDs);
    }

}
