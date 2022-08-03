package com.nationalid.endpoint.service;

import lombok.RequiredArgsConstructor;
import nationalid.SegmentedNationalID;
import nationalid.loggers.LogManager;
import nationalid.verificationapp.IDVerificator;
import nationalid.verificationapp.IO.InputManager;
import nationalid.verificationapp.CategorizedIDLists;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.print.attribute.standard.MediaSize.NA;

import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.loggers.ErrorLogger;
import com.nationalid.endpoint.model.InvalidID;
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
    private final ValidationErrorRepository validationErrorRepository;

    // region ID getters

    public Optional<NationalIDrecord> getIDRecordFromRepo(String ID) {
        Optional<NationalIDrecord> nationalIDRecord = nationalIDRepository.findById(ID);
        if (nationalIDRecord.isEmpty())
            return nationalIDRecord;

        // TODO: fix the mapping, these should map themselves
        getAndSetValidationErrorsForID(nationalIDRecord.get());

        return nationalIDRecord;
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

    private List<ValidationError> getValidationErrorsForID(NationalIDrecord ID) {
        return validationErrorRepository.findAllByNationalID(ID);
    }

    private List<ValidationError> getValidationErrorsForID(List<NationalIDrecord> nationalIDs) {
        List<String> IDs = new ArrayList<>();

        nationalIDs.stream().parallel().forEach(nationalID -> IDs.add(nationalID.getId()));

        return validationErrorRepository.findAllByNationalIDs(IDs);
    }

    private void mapValidationErrorsToNationalIDs(List<NationalIDrecord> IDs) {
        final List<ValidationError> validationErrors;
        validationErrors = getValidationErrorsForID(IDs);

        IDs.stream().parallel().forEach(
                ID -> {
                    ID.setValidationErrors(validationErrors.stream()
                            .filter(error -> error.getNationalID().getId().equals(ID.getId())).toList());
                });
    }

    public List<NationalIDrecord> getExistingRecords(String... ID) {
        return nationalIDRepository.findAllExistingFromList(Arrays.asList(ID));
    }

    private void getAndSetValidationErrorsForID(NationalIDrecord ID) {
        ID.setValidationErrors(getValidationErrorsForID(ID));
    }

    // endregion

    // region Problem Getters

    public String getStatusForID(String ID, Boolean EnsureDataPersistence) {
        Optional<List<String>> problems = getProblemsFromIDRecord(ID);

        if (!problems.isEmpty())
            return formatProblems(ID, problems.get());

        problems = getProblemsBySegmentation(ID, EnsureDataPersistence);

        if (problems.isEmpty())
            return "OK";

        return formatProblems(ID, problems.get());
    }

    private Optional<List<String>> getProblemsFromIDRecord(String ID) {
        Optional<NationalIDrecord> nationalIDRecord = getIDRecordFromRepo(ID);

        if (nationalIDRecord.isEmpty())
            return Optional.empty();

        return Optional.of(nationalIDRecord.get().fetchErrorList());
    }

    private Optional<List<String>> getProblemsBySegmentation(String ID, Boolean EnsureDataPersistence) {
        List<String> problemList = new ArrayList<>();
        Optional<Long> parsedID = ParseIDFromString(ID, EnsureDataPersistence);

        if (parsedID.isEmpty()) {
            problemList.add(String.format("Could not parse the provided ID %s into a number", ID));
            return Optional.of(problemList);
        }

        return getProblemsBySegmentation(parsedID.get(), EnsureDataPersistence);
    }

    private Optional<List<String>> getProblemsBySegmentation(long ID, Boolean EnsureDataPersistence) {
        SegmentedNationalID segmentedID = new SegmentedNationalID(ID);

        if (EnsureDataPersistence)
            SaveNationalID(segmentedID);

        if (segmentedID.VerifyIntegrity())
            return Optional.of(new ArrayList<>());

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

    private List<SegmentedNationalID> SegmentAndSave(Boolean SaveToDB, long... IDs) {
        List<SegmentedNationalID> segmentedIDs = new ArrayList<>();

        Arrays.stream(IDs).parallel().forEach(ID -> {
            SegmentedNationalID segmentedNationalID = new SegmentedNationalID(ID);
            segmentedIDs.add(segmentedNationalID);
            if (SaveToDB)
                SaveNationalID(segmentedNationalID);
        });

        return segmentedIDs;
    }

    private List<SegmentedNationalID> SegmentAndSave(Boolean SaveToDB, List<String> IDs) {
        List<SegmentedNationalID> segmentedIDs = new ArrayList<>();

        IDs.stream().parallel().forEach(ID -> {
            Optional<Long> parsedID = ParseIDFromString(ID, SaveToDB);

            if (!parsedID.isEmpty()) {
                SegmentedNationalID segmentedNationalID = new SegmentedNationalID(parsedID.get());
                segmentedIDs.add(segmentedNationalID);
                if (SaveToDB)
                    SaveNationalID(segmentedNationalID);
            }
        });

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
        SaveNationalID(new NationalIDrecord(invalidID));
    }

    private void SaveNationalID(ValidID validID) {
        SaveNationalID(new NationalIDrecord(validID));
    }

    private void SaveNationalID(NationalIDrecord record) {
        nationalIDRepository.save(record);
        // validationErrorRepository.saveAll(record.getValidationErrors());
    }

    // region validators: all public methods should have an option to ensure data
    // persistence

    public ValidatedIDResponse ValidateIDs(Boolean EnsureDataPersistence, String... IDs) {
        List<NationalIDrecord> nationalIDrecords;
        List<SegmentedNationalID> segmentedNationalIDs;

        try {
            nationalIDrecords = getExistingRecords(IDs);

            mapValidationErrorsToNationalIDs(nationalIDrecords);

            List<String> unregisteredIDs = removeExistingIDs(nationalIDrecords, IDs);

            segmentedNationalIDs = SegmentAndSave(EnsureDataPersistence, unregisteredIDs);

            return ValidatedIDResponse.mergeMultiple(
                    ValidateSegmentedIDs(
                            segmentedNationalIDs),
                    new ValidatedIDResponse(nationalIDrecords));

        } catch (JpaSystemException ex) {
            // TODO: This is a worakound for failing queries during parallel execution.
            // Befor a fix is found, this will just treat all IDs as new

            // TODO add a logging solution with DB manager
            segmentedNationalIDs = SegmentAndSave(EnsureDataPersistence, Arrays.asList(IDs));

            return ValidateSegmentedIDs(segmentedNationalIDs);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public ValidatedIDResponse ValidateProblemsOnIDsFromFile(Boolean EnsureDataPersistence,
            MultipartFile file) throws IOException {
        String content = new String(file.getBytes());

        String[] ParsedIDs = InputManager.cutContentIntoLines(content);

        // TODO: Parsing them X at a time as larger parts cause errors at jsql level.
        int seperateIntoGroupsOf = 100;

        if (ParsedIDs.length <= seperateIntoGroupsOf)
            return ValidateIDs(EnsureDataPersistence, ParsedIDs);

        List<String[]> chunksOfParsedID = new ArrayList<>();
        int NumberOfChunks = (ParsedIDs.length / seperateIntoGroupsOf);
        for (int i = 0; i <= NumberOfChunks; i++) {
            chunksOfParsedID
                    .add(Arrays.copyOfRange(ParsedIDs, i * seperateIntoGroupsOf,
                            NumberOfChunks == i ? ParsedIDs.length : seperateIntoGroupsOf * (i + 1)));
        }

        List<ValidatedIDResponse> validatedIDResponses = new ArrayList<>();

        chunksOfParsedID.stream().parallel().forEach(
                chunk -> validatedIDResponses.add(ValidateIDs(false, chunk)));

        return ValidatedIDResponse.mergeMultiple(validatedIDResponses.toArray(new ValidatedIDResponse[0]));

    }

    public ValidatedIDResponse ValidateIDs(Boolean EnsureDataPersistence, long... IDs) {
        List<SegmentedNationalID> segmentedNationalIDs = new ArrayList<>();
        Arrays.stream(IDs).parallel().forEach(ID -> segmentedNationalIDs.add(new SegmentedNationalID(ID)));
        return ValidateSegmentedIDs(segmentedNationalIDs);
    }

    private ValidatedIDResponse ValidateSegmentedIDs(SegmentedNationalID segmentedNationalID) {
        List<SegmentedNationalID> segmentedNationalIDs = new ArrayList<>();
        segmentedNationalIDs.add(segmentedNationalID);
        return ValidateSegmentedIDs(segmentedNationalIDs);
    }

    private ValidatedIDResponse ValidateSegmentedIDs(List<SegmentedNationalID> segmentedNationalIDs) {

        IDVerificator idVerificator = new IDVerificator();
        CategorizedIDLists categorizedIDLists = idVerificator.VerifyIDs(segmentedNationalIDs);

        ValidatedIDResponse validatedIDResponse = new ValidatedIDResponse(categorizedIDLists);
        return validatedIDResponse;
    }

    // endregion

    // region helpers

    private Optional<Long> ParseIDFromString(String ID, Boolean SaveOnError) {
        Long parsedID;
        try {
            parsedID = Long.parseLong(ID);
        } catch (Exception ex) {
            if (SaveOnError) {
                String problem = String.format("Could not parse the provided ID %s into a number", ID);
                NationalIDrecord problematicID = new NationalIDrecord(ID, problem);
                SaveNationalID(problematicID);
            }
            return Optional.empty();
        }

        return Optional.of(parsedID);
    }

    private List<String> removeExistingIDs(List<NationalIDrecord> existingRecords, String... IDs) {
        List<String> unregisteredIDs = new ArrayList<>();

        Arrays.stream(IDs).parallel().forEach(ID -> {
            if (existingRecords.stream().noneMatch(record -> record.getId().equals(ID))) {
                unregisteredIDs.add(ID);
            }
        });

        return unregisteredIDs;
    }

    // endregion
}
