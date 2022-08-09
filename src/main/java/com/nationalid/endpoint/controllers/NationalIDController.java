package com.nationalid.endpoint.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.NationalIDWithErrors;
import com.nationalid.endpoint.model.responseObjects.ValidID;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDLists;
import com.nationalid.endpoint.service.NationalIDService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nationalids")
public class NationalIDController {

    private final NationalIDService nationalIDService;

    @GetMapping("/{id}")
    public ResponseEntity<NationalIDWithErrors> getRecordById(@PathVariable("id") String id) {
        Optional<NationalIDWithErrors> nationalIDrecord = nationalIDService.getIDWithErrorsFromRepos(id);

        if (nationalIDrecord.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(nationalIDrecord.get());
    }

    @PostMapping("/id")
    public ResponseEntity<String> getVerificationOnStringID(@RequestParam("id") String ID) {
        String responseMessage = nationalIDService.getStatusForID(ID, true);
        if (responseMessage == "OK")
            return ResponseEntity.ok(responseMessage);

        return ResponseEntity.badRequest().body(responseMessage);
    }

    @Data
    private static class IDList {
        private String[] IDs;

    }

    @PostMapping("/ids")
    public ResponseEntity<ValidatedIDLists> getVerificationForMultipleIDs(
            @RequestBody IDList idList) {
        ValidatedIDLists validatedIDResponse = nationalIDService.ValidateIDs(true, idList.getIDs());

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);
    }

    @Data
    private static class PassedDates {
        private LocalDate from;
        private LocalDate to;
    }

    @PostMapping("/search/byDate")
    public ResponseEntity<List<ValidID>> getNationalIDsBasedOnDate(@RequestBody PassedDates passedDates) {
        if (passedDates.getFrom().isAfter(passedDates.getTo()))
            // TODO: added this to avoid unecessary DB calls, but this should be made into a
            // more clear message
            return ResponseEntity.badRequest().build();

        List<ValidID> validIDs = nationalIDService.getValidIDsBasedOnDate(passedDates.getFrom(),
                passedDates.getTo());

        if (validIDs.size() == 0)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(validIDs);
    }

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
