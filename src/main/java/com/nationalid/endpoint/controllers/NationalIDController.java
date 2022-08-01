package com.nationalid.endpoint.controllers;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nationalid.endpoint.model.responseObjects.ValidatedIDResponse;
import com.nationalid.endpoint.service.NationalIDService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nationalids")
public class NationalIDController {

    private final NationalIDService nationalIDService;

    @GetMapping("/")
    public String Default() {
        return "Hi";
    }

    @PostMapping("/id")
    public ResponseEntity<String> getVerificationOnStringID(@RequestParam("id") String stringifiedId) {
        String responseMessage = nationalIDService.VerifyProblemsOnID(stringifiedId);
        if (responseMessage == "OK")
            return ResponseEntity.ok(responseMessage);

        return ResponseEntity.badRequest().body(responseMessage);
    }

    @Data
    private static class IDList {
        private long[] IDs;

    }

    @PostMapping("/ids")
    public ResponseEntity<ValidatedIDResponse> getVerificationForMultipleIDs(
            @RequestBody IDList idList) {
        ValidatedIDResponse validatedIDResponse = nationalIDService.VerifyProblemsOnIDs(idList.getIDs());

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);
    }

    @PostMapping("/idfile")
    public ResponseEntity<ValidatedIDResponse> getVerificationOnIDsFromAFile(
            @RequestParam("file") MultipartFile file)
            throws IOException {
        ValidatedIDResponse validatedIDResponse = nationalIDService.VerifyProblemsOnIDsFromFile(file);

        if (validatedIDResponse.AnyInvalid())
            return ResponseEntity.badRequest().body(validatedIDResponse);

        return ResponseEntity.ok(validatedIDResponse);

    }
}
