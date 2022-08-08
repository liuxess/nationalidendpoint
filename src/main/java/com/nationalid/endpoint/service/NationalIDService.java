package com.nationalid.endpoint.service;

import lombok.RequiredArgsConstructor;
import nationalid.SegmentedNationalID;
import nationalid.verificationapp.IDVerificator;
import nationalid.verificationapp.IO.FileInputManager;
import nationalid.verificationapp.CategorizedIDLists;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.print.attribute.standard.MediaSize.NA;

import org.springframework.boot.context.properties.bind.validation.ValidationErrors;
import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.InvalidID;
import com.nationalid.endpoint.model.NationalIDWithErrors;
import com.nationalid.endpoint.model.ValidID;
import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.entity.ValidationError;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDResponse;
import com.nationalid.endpoint.repositories.NationalIDRepository;
import com.nationalid.endpoint.repositories.ValidationErrorRepository;

@Service
@RequiredArgsConstructor
public class NationalIDService {

    private final NationalIDRepository nationalIDRepository;
    private final ValidationErrorService validationErrorService;

    // region ID getters

    public Optional<NationalIDWithErrors> getIDWithErrorsFromRepos(String ID) {
        Optional<NationalIDrecord> potentialNationalIDRecord = nationalIDRepository.findById(ID);

        if (potentialNationalIDRecord.isEmpty())
            return Optional.empty();

        List<ValidationError> validationErrors = validationErrorService.getValidationErrorsForID(ID);

        NationalIDWithErrors nationalIDWithErrors = new NationalIDWithErrors(potentialNationalIDRecord.get(),
                validationErrors);

        return Optional.of(nationalIDWithErrors);
    }

    public List<ValidID> getValidIDsBasedOnDate(Date startDate, Date endDate) {
        List<NationalIDrecord> nationalIDs = getNationalIDsBasedOnDate(startDate, endDate);
        List<ValidID> validIDs = nationalIDs.stream().parallel().map(record -> new ValidID(record)).toList();
        return validIDs;
    }

    public List<NationalIDrecord> getNationalIDsBasedOnDate(Date startDate, Date endDate) {
        List<NationalIDrecord> nationalIDs = nationalIDRepository.findAllBetweenDates(startDate, endDate);
        return nationalIDs;
    }

    private List<ValidationError> getValidationErrorsForID(List<NationalIDrecord> nationalIDs) {
        List<String> IDs = new ArrayList<>();

        nationalIDs.stream().parallel().forEach(nationalID -> IDs.add(nationalID.getId()));

        return validationErrorService.getValidationErrorsForIDs(IDs);
    }

    private List<NationalIDWithErrors> mapValidationErrorsToNationalIDs(List<NationalIDrecord> IDs) {
        List<ValidationError> validationErrors = getValidationErrorsForID(IDs);
        List<NationalIDWithErrors> nationalIDWithErrors;
        nationalIDWithErrors = new ArrayList<>(IDs.stream().parallel()
                .map(mapIDsToErrors(validationErrors)).toList());
        return nationalIDWithErrors;
    }

    private Function<? super NationalIDrecord, ? extends NationalIDWithErrors> mapIDsToErrors(
            List<ValidationError> validationErrors) {
        return ID -> new NationalIDWithErrors(ID, getErrorsForNationalID(validationErrors, ID));
    }

    private List<ValidationError> getErrorsForNationalID(List<ValidationError> validationErrors, NationalIDrecord ID) {
        return validationErrors.stream().parallel().filter(
                error -> error.getNationalID().equals(ID.getId())).toList();
    }

    public List<NationalIDrecord> getExistingRecords(String... ID) {
        return nationalIDRepository.findAllExistingFromList(Arrays.asList(ID));
    }

    // endregion

    // region Problem Getters

    public String getStatusForID(String ID, Boolean EnsureDataPersistence) {
        // Get a saved version
        Optional<NationalIDWithErrors> optionalNationalID = getIDWithErrorsFromRepos(ID);
        if (optionalNationalID.isPresent()) {
            List<String> problems;
            problems = optionalNationalID.get().getErrors().stream().map(error -> error.getErrorMessage()).toList();
            return formatProblems(ID, problems);
        }

        // else
        Optional<List<String>> problems = getProblemsBySegmentation(ID, EnsureDataPersistence);

        if (problems.isEmpty())
            return "OK";

        return formatProblems(ID, problems.get());
    }

    private Optional<List<String>> getProblemsBySegmentation(String ID, Boolean EnsureDataPersistence) {
        SegmentedNationalID segmentedID = new SegmentedNationalID(ID);

        if (EnsureDataPersistence)
            SaveNationalID(segmentedID);

        if (segmentedID.VerifyIntegrity()) // return empty list
            return Optional.empty();

        return Optional.of(segmentedID.getProblemList());
    }

    private String formatProblems(String ID, List<String> problemList) {

        if (problemList.isEmpty())
            return "OK";

        return String.format("The ID %s was found to have these problems: {%s \r\n}",
                ID, String.join("\r\n", problemList));

    }

    // endregion

    // region Segmentation and Saving

    private List<SegmentedNationalID> SegmentAndSave(Boolean SaveToDB, List<String> IDs) {

        List<SegmentedNationalID> segmentedIDs = IDs.stream().parallel().map(ID -> {

            SegmentedNationalID segmentedNationalID = new SegmentedNationalID(ID);
            if (SaveToDB)
                SaveNationalID(segmentedNationalID);

            return segmentedNationalID;
        }).toList();

        return segmentedIDs;
    }

    private void SaveNationalID(SegmentedNationalID ID) {
        if (ID.VerifyIntegrity()) {
            SaveNationalID(new ValidID(ID));
            return;
        }
        SaveNationalID(new InvalidID(ID));
    }

    private void SaveNationalID(InvalidID invalidID) {
        SaveNationalID(new NationalIDWithErrors(invalidID));
    }

    private void SaveNationalID(ValidID validID) {
        SaveNationalID(new NationalIDrecord(validID));
    }

    private void SaveNationalID(NationalIDrecord record) {
        nationalIDRepository.save(record);
    }

    private void SaveNationalID(NationalIDWithErrors nationalIDWithErrors) {
        nationalIDRepository.save(nationalIDWithErrors.getNationalIDrecord());
        validationErrorService.saveValidationErrors(nationalIDWithErrors.getErrors());
    }

    // region validators: all public methods should have an option to ensure data
    // persistence

    public ValidatedIDResponse ValidateIDs(Boolean EnsureDataPersistence, String... IDs) {
        List<NationalIDrecord> nationalIDrecords = getExistingRecords(IDs);

        List<NationalIDWithErrors> nationalIDsWithErrors = mapValidationErrorsToNationalIDs(nationalIDrecords);

        List<String> unregisteredIDs = Arrays.stream(IDs).parallel()
                .filter(filterOutAlreadyUsedIDs(nationalIDsWithErrors)).toList();

        List<SegmentedNationalID> segmentedNationalIDs = SegmentAndSave(EnsureDataPersistence, unregisteredIDs);

        return ValidatedIDResponse.mergeMultiple(
                ValidateSegmentedIDs(segmentedNationalIDs),
                new ValidatedIDResponse(nationalIDsWithErrors));
    }

    private Predicate<? super String> filterOutAlreadyUsedIDs(List<NationalIDWithErrors> nationalIDsWithErrors) {
        return ID -> nationalIDsWithErrors.stream().parallel()
                .noneMatch(nationalIDWithErrors -> nationalIDWithErrors.getNationalIDrecord().getId().equals(ID));
    }

    public ValidatedIDResponse ValidateProblemsOnIDsFromFile(Boolean EnsureDataPersistence,
            MultipartFile file) throws IOException {
        String content = new String(file.getBytes());

        String[] ParsedIDs = FileInputManager.cutContentIntoLines(content);

        // TODO: Parsing them X at a time as larger parts cause errors at jsql level.
        // int seperateIntoGroupsOf = 100;

        // if (ParsedIDs.length <= seperateIntoGroupsOf)
        // return ValidateIDs(EnsureDataPersistence, ParsedIDs);

        // List<String[]> chunksOfParsedID = new ArrayList<>();
        // int NumberOfChunks = (ParsedIDs.length / seperateIntoGroupsOf);
        // for (int i = 0; i <= NumberOfChunks; i++) {
        // chunksOfParsedID
        // .add(Arrays.copyOfRange(ParsedIDs, i * seperateIntoGroupsOf,
        // NumberOfChunks == i ? ParsedIDs.length - 1 : seperateIntoGroupsOf * (i +
        // 1)));
        // }

        // List<ValidatedIDResponse> validatedIDResponses =
        // chunksOfParsedID.stream().parallel().map(
        // chunk -> ValidateIDs(EnsureDataPersistence, chunk)).toList();

        // return ValidatedIDResponse.mergeMultiple(validatedIDResponses.toArray(new
        // ValidatedIDResponse[0]));

        return ValidateIDs(EnsureDataPersistence, ParsedIDs);

    }

    private ValidatedIDResponse ValidateSegmentedIDs(List<SegmentedNationalID> segmentedNationalIDs) {
        CategorizedIDLists categorizedIDLists = IDVerificator.VerifyIDs(segmentedNationalIDs);

        ValidatedIDResponse validatedIDResponse = new ValidatedIDResponse(categorizedIDLists);
        return validatedIDResponse;
    }

    // endregion

    // region helpers

    // endregion
}
