package com.nationalid.endpoint.model.responseObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.nationalid.endpoint.model.NationalIDWithErrors;

import lombok.Getter;
import nationalid.verificationapp.CategorizedIDLists;

@Getter
public class ValidatedIDLists {

    int total;
    int numberOfValidIDs;
    final List<ValidID> validIDs;
    final List<InvalidID> invalidIDs;

    public ValidatedIDLists(CategorizedIDLists categorizedIDLists) {

        validIDs = new ArrayList<>(
                categorizedIDLists.getCorrect().stream().parallel().map(correctID -> new ValidID(correctID)).toList());

        invalidIDs = new ArrayList<>(categorizedIDLists.getIncorrect().stream().parallel()
                .map(incorrectID -> new InvalidID(incorrectID)).toList());

        RecalculateTotals();
    }

    public ValidatedIDLists(ArrayList<ValidatedIDBase> validatedIDs) {
        Map<Boolean, List<ValidatedIDBase>> SplitIDs = validatedIDs.stream().parallel()
                .collect(Collectors.partitioningBy(ID -> ID.IsValid()));

        this.validIDs = new ArrayList<>(SplitIDs.get(true).stream().map(ID -> (ValidID) ID).toList());
        this.invalidIDs = new ArrayList<>(SplitIDs.get(false).stream().map(ID -> (InvalidID) ID).toList());
        RecalculateTotals();
    }

    public ValidatedIDLists(List<ValidID> validIDs, List<InvalidID> invalidIDs) {
        this.validIDs = validIDs;
        this.invalidIDs = invalidIDs;
        RecalculateTotals();
    }

    public ValidatedIDLists(List<NationalIDWithErrors> nationalIDsWithErrors) {
        List<ValidatedIDBase> validatedIDs = nationalIDsWithErrors.stream().parallel().map(ID -> {
            if (ID.HasErrors())
                return new InvalidID(ID);

            return new ValidID(ID);
        }).toList();

        Map<Boolean, List<ValidatedIDBase>> SplitIDs = validatedIDs.stream().parallel()
                .collect(Collectors.partitioningBy(ID -> ID.IsValid()));

        this.validIDs = new ArrayList<>(SplitIDs.get(true).stream().map(ID -> (ValidID) ID).toList());
        this.invalidIDs = new ArrayList<>(SplitIDs.get(false).stream().map(ID -> (InvalidID) ID).toList());

        RecalculateTotals();
    }

    private void RecalculateTotals() {
        numberOfValidIDs = validIDs.size();
        total = numberOfValidIDs + invalidIDs.size();
    }

    public Boolean AnyInvalid() {
        return !invalidIDs.isEmpty();
    }

    public static ValidatedIDLists mergeMultiple(ValidatedIDLists... responses) {
        final List<ValidID> validIDs = new ArrayList<>();
        final List<InvalidID> invalidIDs = new ArrayList<>();

        Arrays.stream(responses).sequential().forEach(
                response -> {
                    validIDs.addAll(response.getValidIDs());
                    invalidIDs.addAll(response.getInvalidIDs());
                });

        return new ValidatedIDLists(validIDs, invalidIDs);
    }

}
