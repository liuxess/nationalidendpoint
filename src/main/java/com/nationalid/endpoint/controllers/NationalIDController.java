package com.nationalid.endpoint.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.DTOs.NationalIDDTO;
import com.nationalid.endpoint.model.DTOs.ValidID;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDLists;
import com.nationalid.endpoint.service.NationalIDService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/*
 * Controller for managing operations concerning NationalIDs
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nationalids")
public class NationalIDController {

    private final NationalIDService nationalIDService;

    /**
     * Attempts to find a requested ID
     * throws a 404 if the requested ID is not found
     * 
     * @param id to be searched for
     * @return Response Entity with 200 or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<NationalIDDTO> getRecordById(@PathVariable("id") String id) {
        Optional<NationalIDDTO> DTO = nationalIDService.getIDDTOFromRepos(id);

        if (DTO.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(DTO.get());
    }

    /**
     * Attempts to verify whether the passed ID is correct or not
     * Returns as bad request or OK
     * 
     * @param ID to check
     * @return 200 with "OK" or bad request
     */
    @PostMapping("/id")
    public ResponseEntity<String> getVerificationOnStringID(@RequestParam("id") String ID) {
        String responseMessage = nationalIDService.getStatusForID(ID, true);
        if (responseMessage == "OK")
            return ResponseEntity.ok(responseMessage);

        return ResponseEntity.badRequest().body(responseMessage);
    }

    /** private class to hold an array of passed IDs */
    @Data
    private static class IDList {
        private String[] IDs;

    }

    /**
     * Tries to verify a list of IDs received
     * 
     * @param idList received ID list
     * @return 200 if all IDs are Valid; bad request if there are any bad ones
     */
    @PostMapping("/ids")
    public ResponseEntity<ValidatedIDLists> getVerificationForMultipleIDs(
            @RequestBody IDList idList) {
        ValidatedIDLists validatedIDResponse = nationalIDService.ValidateIDs(true, idList.getIDs());

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);
    }

    /** private class to abstract two date passing */
    @Data
    private static class PassedDates {
        private LocalDate from;
        private LocalDate to;
    }

    /**
     * Lists all Valid IDs from the database that repesent birth between the two
     * passed dated
     * 
     * @param passedDates to be compared against
     * @return bad request if the dates are not correct; 404 if there are no
     *         registered IDs between the dates; 200 with the list.
     */
    @PostMapping("/search/byDate")
    public ResponseEntity<List<ValidID>> getNationalIDsBasedOnDate(@RequestBody PassedDates passedDates) {
        if (passedDates.getFrom().isAfter(passedDates.getTo()))
            return ResponseEntity.badRequest().build();

        List<ValidID> validIDs = nationalIDService.getValidIDsBasedOnDate(passedDates.getFrom(),
                passedDates.getTo());

        if (validIDs.size() == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(validIDs);
    }

    /**
     * Tries to parse a file row by row, and validate an ID out of each row
     * 
     * @param file file that has an ID in each row without additional seperators
     * @return 200 if all IDs are valid, bad request if there are invaldi IDs
     * @throws IOException if The file could not be read
     */
    @PostMapping("/idfile")
    public ResponseEntity<ValidatedIDLists> getVerificationOnIDsFromAFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {
        ValidatedIDLists validatedIDResponse = nationalIDService.ValidateProblemsOnIDsFromFile(true, file);

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);

    }
}
