package com.nationalid.endpoint.service;

import lombok.RequiredArgsConstructor;
import nationalid.SegmentedNationalID;
import nationalid.verificationapp.IDVerificator;
import nationalid.verificationapp.IO.FileInputManager;
import nationalid.verificationapp.CategorizedIDLists;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.DTOs.InvalidID;
import com.nationalid.endpoint.model.DTOs.NationalIDDTO;
import com.nationalid.endpoint.model.DTOs.ValidID;
import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.entity.ValidationError;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDLists;
import com.nationalid.endpoint.repositories.NationalIDRepository;

/**
 * Service for national ID operations
 */
@Service
@RequiredArgsConstructor
public class NationalIDService {

    private final NationalIDRepository nationalIDRepository;
    private final ValidationErrorService validationErrorService;

    // region ID getters

    /**
     * Attempts to retrieve a DTO containing national ID and it's errors from the
     * database
     * 
     * @param ID to retrieve
     * @return DTO if ID is found; Empty if not found
     * 
     * @see com.nationalid.endpoint.model.DTOs.NationalIDDTO
     */
    public Optional<NationalIDDTO> getIDDTOFromRepos(String ID) {
        Optional<NationalIDrecord> potentialNationalIDRecord = nationalIDRepository.findById(ID);

        if (potentialNationalIDRecord.isEmpty())
            return Optional.empty();

        List<ValidationError> validationErrors = validationErrorService.getValidationErrorsForID(ID);

        NationalIDDTO nationalIDDTO = new NationalIDDTO(potentialNationalIDRecord.get(),
                validationErrors);

        return Optional.of(nationalIDDTO);
    }

    /**
     * Retrieves a list of valid IDs based on birth dates
     * 
     * @param startDate earliest birth date
     * @param endDate   latest birth date
     * @return List of IDs in between the two dates
     * @see com.nationalid.endpoint.model.DTOs.ValidID
     */
    public List<ValidID> getValidIDsBasedOnDate(LocalDate startDate, LocalDate endDate) {
        List<NationalIDrecord> nationalIDs = getNationalIDsBasedOnDate(startDate, endDate);
        List<ValidID> validIDs = nationalIDs.stream().parallel().map(record -> new ValidID(record)).toList();
        return validIDs;
    }

    /**
     * Retrieves a list of NationalID records based on birth dates
     * 
     * @param startDate earliest birth date
     * @param endDate   latest birth date
     * @return List of NationalID records
     * 
     * @see com.nationalid.endpoint.model.entity.NationalIDrecord
     */
    public List<NationalIDrecord> getNationalIDsBasedOnDate(LocalDate startDate, LocalDate endDate) {
        List<NationalIDrecord> nationalIDs = nationalIDRepository.findAllBetweenDates(startDate, endDate);
        return nationalIDs;
    }

    /**
     * Retrieves a list of Validation Error records from the database based on
     * nationalIDs
     * 
     * @param nationalIDs for which errors should be fetched
     * @return List of corresponding Validation Errors
     * 
     * @see com.nationalid.endpoint.model.entity.NationalIDrecord
     * @see com.nationalid.endpoint.model.entity.ValidationError
     */
    private List<ValidationError> getValidationErrorsForID(List<NationalIDrecord> nationalIDs) {
        List<String> IDs = new ArrayList<>(
                nationalIDs.stream().parallel()
                        .map(nationalID -> nationalID.getId()).toList());

        return validationErrorService.getValidationErrorsForIDs(IDs);
    }

    /**
     * Maps validation errors from teh databse to National ID records, and returns
     * the DTO for it
     * 
     * @param IDs to map validation errors to
     * @return List of National ID DTOs
     * 
     * 
     * @see com.nationalid.endpoint.model.entity.NationalIDrecord
     * @see com.nationalid.endpoint.model.entity.ValidationError
     * @see com.nationalid.endpoint.model.DTOs.NationalIDDTO
     */
    private List<NationalIDDTO> mapValidationErrorsToNationalIDs(List<NationalIDrecord> IDs) {
        List<ValidationError> validationErrors = getValidationErrorsForID(IDs);
        Map<String, List<ValidationError>> mappedValidationErrors = validationErrors.stream()
                .collect(Collectors.groupingBy(ValidationError::getNationalID));

        List<NationalIDDTO> nationalIDDTOs;
        nationalIDDTOs = new ArrayList<>(IDs.stream().parallel()
                .map(ID -> new NationalIDDTO(ID, mappedValidationErrors.get(ID.getId().intern()))).toList());
        return nationalIDDTOs;
    }

    /**
     * Retrieves a list of National ID records that exist in the database based on
     * the IDs provided
     * 
     * @param IDs to retrieve
     * @return National ID records from DB
     */
    public List<NationalIDrecord> getExistingRecords(String... IDs) {
        return nationalIDRepository.findAllExistingFromList(Arrays.asList(IDs));
    }

    // endregion

    // region Problem Getters

    /**
     * Retrieves the Status of ID as text;
     * 
     * @param ID                    to manage
     * @param EnsureDataPersistence whether to save if the ID does not exist in the
     *                              DB
     * @return "OK" if there are no problems; Formatted problem list if there's any
     */
    public String getStatusForID(String ID, Boolean EnsureDataPersistence) {
        // Get a saved version
        Optional<NationalIDDTO> optionalNationalID = getIDDTOFromRepos(ID);
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

    /**
     * Gets the problem list by Segmenting the ID
     * 
     * @param ID                    to segment
     * @param EnsureDataPersistence whether to save the segmented ID
     * @return List of problems; if none, returns empty;
     * 
     * @see nationalid.SegmentedNationalID
     */
    private Optional<List<String>> getProblemsBySegmentation(String ID, Boolean EnsureDataPersistence) {
        SegmentedNationalID segmentedID = new SegmentedNationalID(ID);

        if (EnsureDataPersistence)
            SaveNationalID(segmentedID);

        if (segmentedID.VerifyIntegrity()) // return empty list
            return Optional.empty();

        return Optional.of(segmentedID.getProblemList());
    }

    /**
     * Formats ID and problem list into comprehensible single String
     * 
     * @param ID          that the problems belong to
     * @param problemList to compile
     * @return "OK" if the list is empty; Otherwise, compiled problem string
     * 
     */
    private String formatProblems(String ID, List<String> problemList) {

        if (problemList.isEmpty())
            return "OK";

        return String.format("The ID %s was found to have these problems: {\r\n%s \r\n}",
                ID, String.join("\r\n", problemList));

    }

    // endregion

    // region Segmentation and Saving

    /**
     * Segments the passed list of IDs and saves them to the DB if required
     * 
     * @param SaveToDB whether the records should be saved
     * @param IDs      to segment
     * @return list of segmented IDs
     * 
     * @see nationalid.SegmentedNationalID
     */
    private List<SegmentedNationalID> SegmentAndSave(Boolean SaveToDB, List<String> IDs) {

        List<SegmentedNationalID> segmentedIDs = IDs.stream().parallel()
                .map(ID -> new SegmentedNationalID(ID))
                .toList();

        if (SaveToDB)
            SaveNationalIDs(segmentedIDs.toArray(new SegmentedNationalID[0]));

        return segmentedIDs;
    }

    /**
     * Saves the ID to the Database
     * 
     * @param ID to save
     * @see nationalid.SegmentedNationalID
     */
    private void SaveNationalID(SegmentedNationalID ID) {
        if (ID.VerifyIntegrity()) {
            SaveNationalID(new ValidID(ID));
            return;
        }
        SaveNationalID(new InvalidID(ID));
    }

    /**
     * Saves the invalid ID with it's errors to the DB
     * 
     * @param invalidID to save
     * 
     * @see com.nationalid.endpoint.model.DTOs.InvalidID
     */
    private void SaveNationalID(InvalidID invalidID) {
        SaveNationalID(new NationalIDDTO(invalidID));
    }

    /**
     * Saves the ID to the DB
     * 
     * @param validID to save
     * 
     * @see com.nationalid.endpoint.model.DTOs.ValidID
     */
    private void SaveNationalID(ValidID validID) {
        SaveNationalID(new NationalIDrecord(validID));
    }

    /**
     * Saves the ID record to the DB
     * 
     * @param record to save
     */
    private void SaveNationalID(NationalIDrecord record) {
        nationalIDRepository.save(record);
    }

    /**
     * Saves the National ID record and it's errors, if any, to the DB
     * 
     * @param nationalIDDTO
     * 
     * @see com.nationalid.endpoint.model.DTOs.NationalIDDTO
     */
    private void SaveNationalID(NationalIDDTO nationalIDDTO) {
        nationalIDRepository.save(nationalIDDTO.getNationalIDrecord());
        validationErrorService.saveValidationErrors(nationalIDDTO.getErrors());
    }

    /**
     * Saves the array of Segmented IDs to the DB
     * 
     * @param segmentedIDs
     */
    private void SaveNationalIDs(SegmentedNationalID[] segmentedIDs) {
        List<NationalIDDTO> nationalIDDTOs;
        nationalIDDTOs = Arrays.stream(segmentedIDs).parallel()
                .map(segmentedID -> {
                    if (segmentedID.VerifyIntegrity())
                        return new NationalIDDTO(new ValidID(segmentedID));

                    return new NationalIDDTO(new InvalidID(segmentedID));
                }).toList();

        SaveNationalIDs(nationalIDDTOs);
    }

    /**
     * Will save all the National IDs and their errors from the DTO list
     * 
     * @param nationalIDDTOs to save
     */
    private void SaveNationalIDs(List<NationalIDDTO> nationalIDDTOs) {
        nationalIDRepository.saveAll(nationalIDDTOs.stream().map(ID -> ID.getNationalIDrecord()).toList());
        validationErrorService.saveValidationErrors(
                nationalIDDTOs.stream().map(ID -> ID.getErrors()).flatMap(List::stream)
                        .collect(Collectors.toList()));
    }

    // region validators: all public methods should have an option to ensure data
    // persistence

    /**
     * Validates IDs from an array and saves them to the DB if needed
     * 
     * @param EnsureDataPersistence whether to save to the DB ot not
     * @param IDs                   to validate
     * @return Validated Lists
     * 
     * @see com.nationalid.endpoint.model.responseObjects.ValidatedIDLists
     */
    public ValidatedIDLists ValidateIDs(Boolean EnsureDataPersistence, String... IDs) {
        List<NationalIDrecord> nationalIDrecords = getExistingRecords(IDs);

        List<NationalIDDTO> nationalIDDTOs = mapValidationErrorsToNationalIDs(nationalIDrecords);

        List<String> unregisteredIDs = Arrays.stream(IDs).parallel()
                .filter(IDIsWithinProvidedList(nationalIDDTOs)).toList();

        List<SegmentedNationalID> segmentedNationalIDs = SegmentAndSave(EnsureDataPersistence, unregisteredIDs);

        return ValidatedIDLists.mergeMultiple(
                ValidateSegmentedIDs(segmentedNationalIDs),
                new ValidatedIDLists(nationalIDDTOs));
    }

    /**
     * @param nationalIDDTOs list to check against
     * @return whether or not the ID is already within the object list
     */
    private Predicate<? super String> IDIsWithinProvidedList(List<NationalIDDTO> nationalIDDTOs) {
        return ID -> nationalIDDTOs.stream().parallel()
                .noneMatch(DTO -> DTO.getNationalIDrecord().getId().equals(ID));
    }

    /**
     * Validates IDs passed within the file and saves new ones if needed
     * 
     * @param EnsureDataPersistence whether to save the data
     * @param file                  to be parsed line by line for IDs
     * @return Validated ID list
     * @throws IOException on errors reading the file
     * @see com.nationalid.endpoint.model.responseObjects.ValidatedIDLists
     */
    public ValidatedIDLists ValidateProblemsOnIDsFromFile(Boolean EnsureDataPersistence,
            MultipartFile file) throws IOException {
        String content = new String(file.getBytes());

        String[] ParsedIDs = FileInputManager.cutContentIntoLines(content);

        return ValidateIDs(EnsureDataPersistence, ParsedIDs);

    }

    /**
     * Alternative method for large file parsing by cutting into smaller chunks.
     * Takes longer, but is more stable.
     * 
     * @param EnsureDataPersistence whether or not to save new records to the DB
     * @param file                  to be parsed line by line for IDs
     * @return Validated ID list
     * @throws IOException on errors reading the file
     * @see com.nationalid.endpoint.model.responseObjects.ValidatedIDLists
     */
    public ValidatedIDLists ValidateProblemsOnIDsFromFileChunked(Boolean EnsureDataPersistence,
            MultipartFile file) throws IOException {
        String content = new String(file.getBytes());

        String[] ParsedIDs = FileInputManager.cutContentIntoLines(content);

        // TODO: Parsing them X at a time as larger parts cause errors at jsql level.
        int seperateIntoGroupsOf = 100;

        if (ParsedIDs.length <= seperateIntoGroupsOf)
            return ValidateIDs(EnsureDataPersistence, ParsedIDs);

        List<String[]> chunksOfParsedID = ParseChunks(ParsedIDs, seperateIntoGroupsOf);

        List<ValidatedIDLists> validatedIDResponses = chunksOfParsedID.stream().sequential().map(
                chunk -> ValidateIDs(EnsureDataPersistence, chunk)).toList();

        return ValidatedIDLists.mergeMultiple(validatedIDResponses.toArray(new ValidatedIDLists[0]));

    }

    /**
     * Parses a single array into a list of chunks of predetermined size
     * 
     * @param ParsedIDs            to seperate
     * @param seperateIntoGroupsOf the size of a single chunk
     * @return List of chunks containing string arrays.
     */
    private List<String[]> ParseChunks(String[] ParsedIDs, int seperateIntoGroupsOf) {
        List<String[]> chunksOfParsedID = new ArrayList<>();
        int NumberOfChunks = (ParsedIDs.length / seperateIntoGroupsOf);
        for (int i = 0; i <= NumberOfChunks; i++) {
            chunksOfParsedID.add(
                    CopyRange(ParsedIDs, seperateIntoGroupsOf, NumberOfChunks, i));
        }
        return chunksOfParsedID;
    }

    /**
     * Wrapper for Arrays.copyOfRange that conducts behind the scenes calculation to
     * be used with chunks of data and for loops
     * Acts as similar to pagination for array
     * 
     * @param ParsedIDs            where to copy the range from; OG array
     * @param seperateIntoGroupsOf size of a chunk
     * @param NumberOfChunks       total number of chunks
     * @param i                    current loop iteration
     * @return current chunk of ID array
     */
    private String[] CopyRange(String[] ParsedIDs, int seperateIntoGroupsOf, int NumberOfChunks, int i) {
        return Arrays.copyOfRange(
                ParsedIDs,
                i * seperateIntoGroupsOf,
                NumberOfChunks == i
                        ? ParsedIDs.length - 1
                        : seperateIntoGroupsOf * (i + 1));
    }

    /**
     * Validates segmented ID List
     * 
     * @param segmentedNationalIDs to validate
     * @return Validated ID List
     * @see com.nationalid.endpoint.model.responseObjects.ValidatedIDLists
     */
    private ValidatedIDLists ValidateSegmentedIDs(List<SegmentedNationalID> segmentedNationalIDs) {
        CategorizedIDLists categorizedIDLists = IDVerificator.VerifyIDs(segmentedNationalIDs);

        ValidatedIDLists validatedIDResponse = new ValidatedIDLists(categorizedIDLists);
        return validatedIDResponse;
    }

    // endregion

    // region Deletion

    /**
     * Deletes all records that have the corresponding ID from the list
     * 
     * @param IDs to delete
     */
    public void deleteBasedOnIDs(List<String> IDs) {
        nationalIDRepository.deleteAllByIdInBatch(IDs);
    }

    /**
     * Deletes all IDs that have problems
     * 
     * @return number of rows deleted
     */
    public int deleteAllIncorrect() {
        List<String> IncorrectIDs = validationErrorService.fetchUniqueNationalIDs();
        deleteBasedOnIDs(IncorrectIDs);
        return IncorrectIDs.size();
    }

    // endregion
}
