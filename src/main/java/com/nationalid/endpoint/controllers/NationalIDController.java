package com.nationalid.endpoint.controllers;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.entity.NationalIDrecord;
import com.nationalid.endpoint.model.responseObjects.ValidatedIDResponse;
import com.nationalid.endpoint.service.NationalIDService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nationalids")
public class NationalIDController {

    private final NationalIDService nationalIDService;

    @GetMapping("/{id}")
    public ResponseEntity<NationalIDrecord> getRecordById(@PathVariable("id") String id) {
        Optional<NationalIDrecord> nationalIDrecord = nationalIDService.getIDRecordFromRepo(id);

        if (nationalIDrecord.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(nationalIDrecord.get());
    }

    @PostMapping("/id")
    public ResponseEntity<String> getVerificationOnStringID(@RequestParam("id") String stringifiedId) {
        String responseMessage = nationalIDService.getStatusForID(stringifiedId, true);
        if (responseMessage == "OK")
            return ResponseEntity.ok(responseMessage);

        return ResponseEntity.badRequest().body(responseMessage);
    }

    @Data
    private static class IDList {
        private String[] IDs;

    }

    @PostMapping("/ids")
    public ResponseEntity<ValidatedIDResponse> getVerificationForMultipleIDs(
            @RequestBody IDList idList) {
        ValidatedIDResponse validatedIDResponse = nationalIDService.ValidateIDs(true, idList.getIDs());

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);
    }

    @PostMapping("/idfile")
    public ResponseEntity<ValidatedIDResponse> getVerificationOnIDsFromAFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {
        ValidatedIDResponse validatedIDResponse = nationalIDService.ValidateProblemsOnIDsFromFile(true, file);

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);

    }
}
