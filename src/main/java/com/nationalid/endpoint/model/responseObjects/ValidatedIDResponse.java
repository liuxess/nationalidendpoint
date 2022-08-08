package com.nationalid.endpoint.model.responseObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.mapping.Map;

import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.NationalIDWithErrors;
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

        validIDs = new ArrayList<>(
                categorizedIDLists.getCorrect().stream().parallel().map(correctID -> new ValidID(correctID)).toList());

        invalidIDs = new ArrayList<>(categorizedIDLists.getIncorrect().stream().parallel()
                .map(incorrectID -> new InvalidID(incorrectID)).toList());

        RecalculateTotals();
    }

    public ValidatedIDResponse(List<ValidID> validIDs, List<InvalidID> invalidIDs) {
        this.validIDs = validIDs;
        this.invalidIDs = invalidIDs;
        RecalculateTotals();
    }

    public ValidatedIDResponse(List<NationalIDWithErrors> nationalIDsWithErrors) {
        validIDs = new ArrayList<>();
        invalidIDs = new ArrayList<>();

        nationalIDsWithErrors.stream().parallel().forEach(
                ID -> {
                    if (ID.HasErrors()) {
                        invalidIDs.add(new InvalidID(ID));
                    } else {
                        validIDs.add(new ValidID(ID));
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
