package com.gabrigiunchi.backendtesi.controller;

import com.gabrigiunchi.backendtesi.exceptions.AccessDeniedException;
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException;
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException;
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error")
public class ResourceControllerAdvice {
    @ResponseBody
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors resourceNotFoundExceptionHandler(ResourceNotFoundException ex) {
        return new VndErrors("Not Found Error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    VndErrors resourceAlreadyExistsExceptionHandler(ResourceAlreadyExistsException ex) {
        return new VndErrors("Already Exist Error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors badRequestExceptionHandler(BadRequestException ex) {
        return new VndErrors("Bad Request", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    VndErrors accessDeniedExceptionHandler(AccessDeniedException ex) {
        return new VndErrors("Forbidden", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    VndErrors badCredentialsExceptionHandler(BadCredentialsException ex) {
        return new VndErrors("Bad credentials", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors invalidArgumentHandler(IllegalArgumentException ex) {
        return new VndErrors("Illegal arguments", ex.getMessage());
    }
}
