package com.nationalid.endpoint.model.responseObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nationalid.endpoint.model.DTOs.InvalidID;
import com.nationalid.endpoint.model.DTOs.NationalIDDTO;
import com.nationalid.endpoint.model.DTOs.ValidID;

import lombok.Getter;
import nationalid.verificationapp.CategorizedIDLists;

/**
 * Lists for IDs that have been validated.
 * Holds Valid and Invalid IDs in seperate lists
 * 
 * @see ValidID
 * @see InvalidID
 */
@Getter
public class ValidatedIDLists {

    int total;
    int numberOfValidIDs;
    final List<ValidID> validIDs;
    final List<InvalidID> invalidIDs;

    /**
     * Wraps categorized segmented IDs to valid and invalid IDs
     * 
     * @param categorizedIDLists categorized segmented IDs
     * @see nationalid.verificationapp.CategorizedIDLists
     */
    public ValidatedIDLists(CategorizedIDLists categorizedIDLists) {

        validIDs = new ArrayList<>(
                categorizedIDLists.getCorrect().stream().parallel().map(correctID -> new ValidID(correctID)).toList());

        invalidIDs = new ArrayList<>(categorizedIDLists.getIncorrect().stream().parallel()
                .map(incorrectID -> new InvalidID(incorrectID)).toList());

        RecalculateTotals();
    }

    /**
     * Takes a list of already validated IDs and maps them to corresponding lists
     * 
     * @param validatedIDs to map out
     */
    public ValidatedIDLists(ArrayList<ValidatedIDBase> validatedIDs) {
        Map<Boolean, List<ValidatedIDBase>> SplitIDs = validatedIDs.stream().parallel()
                .collect(Collectors.partitioningBy(ID -> ID.IsValid()));

        this.validIDs = new ArrayList<>(SplitIDs.get(true).stream().map(ID -> (ValidID) ID).toList());
        this.invalidIDs = new ArrayList<>(SplitIDs.get(false).stream().map(ID -> (InvalidID) ID).toList());
        RecalculateTotals();
    }

    /**
     * Create a common object for lists
     * 
     * @param validIDs
     * @param invalidIDs
     */
    public ValidatedIDLists(List<ValidID> validIDs, List<InvalidID> invalidIDs) {
        this.validIDs = validIDs;
        this.invalidIDs = invalidIDs;
        RecalculateTotals();
    }

    /**
     * split Valid and Invalid National IDs into internal lists
     * 
     * @param nationalIDDTOs to split
     */
    public ValidatedIDLists(List<NationalIDDTO> nationalIDDTOs) {
        List<ValidatedIDBase> validatedIDs = nationalIDDTOs.stream().parallel().map(ID -> {
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

    /**
     * @return whether there are any invalid IDs
     */
    public Boolean AnyInvalid() {
        return !invalidIDs.isEmpty();
    }

    /**
     * Merges multiple lists of valid and invalid IDs into single coupling
     * 
     * @param validatedIDlists to merge into one
     * @return single instance with Valid and Invalid lists
     */
    public static ValidatedIDLists mergeMultiple(ValidatedIDLists... validatedIDlists) {
        final List<ValidID> validIDs = new ArrayList<>();
        final List<InvalidID> invalidIDs = new ArrayList<>();

        Arrays.stream(validatedIDlists).sequential().forEach(
                list -> {
                    validIDs.addAll(list.getValidIDs());
                    invalidIDs.addAll(list.getInvalidIDs());
                });

        return new ValidatedIDLists(validIDs, invalidIDs);
    }

}
