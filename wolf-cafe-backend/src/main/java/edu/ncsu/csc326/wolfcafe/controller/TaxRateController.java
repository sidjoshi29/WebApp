package edu.ncsu.csc326.wolfcafe.controller;

import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.service.TaxRateService;
import lombok.AllArgsConstructor;

/**
 * Controller for Tax Rate functionality for WolfCafe.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/taxRate" )
@AllArgsConstructor
public class TaxRateController {

    /** Link to TaxRateService */
    private final TaxRateService taxRateService;

    /**
     * Returns the tax rate. Any one can do this since it needs to be called in
     * the frontend (to add to the total) no matter who the user is
     *
     * @return a the current tax rate
     */
    @GetMapping
    @PreAuthorize ( "hasAnyRole('STAFF', 'CUSTOMER', 'ADMIN')" )
    public ResponseEntity< ? > getTaxRate () {
        final double rate = taxRateService.getTaxRate();
        return ResponseEntity.ok( Collections.singletonMap( "rate", rate ) );
    }

    /**
     * Sets the tax rate. Requires the ADMIN role.
     *
     * @return whether or not the update was successful
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @PutMapping
    public ResponseEntity< ? > setTaxRate ( @RequestBody final Double rate ) {

        final boolean success = taxRateService.setTaxRate( rate );
        return ResponseEntity.ok( Collections.singletonMap( "success", success ) );

    }
}
