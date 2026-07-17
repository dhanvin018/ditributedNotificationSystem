package com.notificationapp.eventposting;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EventRequest(
        @NotBlank @Email String email,
        @NotBlank String message) {}