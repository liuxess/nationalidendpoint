package com.nationalid.endpoint.service;

import lombok.RequiredArgsConstructor;
import nationalid.SegmentedNationalID;
import nationalid.loggers.LogManager;
import nationalid.verificationapp.IDVerificator;
import nationalid.verificationapp.IO.InputManager;
import nationalid.verificationapp.CategorizedIDLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.loggers.ErrorLogger;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDResponse;

@Service
@RequiredArgsConstructor
public class NationalIDService {

    public List<String> getProblemsFromID(long ID) {
        SegmentedNationalID segmentedID = new SegmentedNationalID(ID);
        if (segmentedID.VerifyIntegrity())
            return new ArrayList<>();

        return segmentedID.getProblemList();
    }

    public String VerifyProblemsOnID(long ID) {
        List<String> problemList = getProblemsFromID(ID);

        if (problemList.isEmpty())
            return "OK";

        return String.format("The ID %s was found to have these problems: {%s \r\n}", String.join("\r\n", problemList));

    }

    public String VerifyProblemsOnID(String ID) {

        Long parsedID;
        try {
            parsedID = Long.parseLong(ID);
        } catch (Exception ex) {
            return String.format("Could not parse the provided ID %s into a number", ID);
        }

        List<String> problemList = getProblemsFromID(parsedID);

        if (problemList.isEmpty())
            return "OK";

        return String.format("The ID %s was found to have these problems: {\r\n\t%s\r\n}", ID,
                String.join("\r\n\t", problemList));

    }

    public ValidatedIDResponse VerifyProblemsOnIDs(long... IDs) {

        List<SegmentedNationalID> segmentedNationalIDs = new ArrayList<>();

        Arrays.stream(IDs).parallel().forEach(ID -> segmentedNationalIDs.add(new SegmentedNationalID(ID)));

        return VerifySegemntedIDs(segmentedNationalIDs);
    }

    private ValidatedIDResponse VerifySegemntedIDs(List<SegmentedNationalID> segmentedNationalIDs) {

        IDVerificator idVerificator = new IDVerificator();
        CategorizedIDLists categorizedIDLists = idVerificator.VerifyIDs(segmentedNationalIDs);

        ValidatedIDResponse validatedIDResponse = new ValidatedIDResponse(categorizedIDLists);
        return validatedIDResponse;
    }

    public ValidatedIDResponse VerifyProblemsOnIDsFromFile(MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        ErrorLogger errorLogger = new ErrorLogger();
        // TODO: do something with the error logger; Should be part of the body returned
        List<SegmentedNationalID> segmentedNationalIDs = InputManager.ReadIDsFromFromString(new LogManager(errorLogger),
                content);

        return VerifySegemntedIDs(segmentedNationalIDs);
    }
}
