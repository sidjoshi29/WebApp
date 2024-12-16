package edu.ncsu.csc326.wolfcafe.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;

/*
 * Tests TaxRateService
 */
@SpringBootTest
class TaxRateServiceTest {

    /*
     * Connection to tax rate service
     */
    @Autowired
    private TaxRateService taxRateService;

    /*
     * Connection to tax rate repository
     */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /*
     * Makes sure the tax rate is set to the default rate before tests
     */
    @BeforeEach
    void setUp () {
        // Clear the repository and set a default tax rate for consistency in
        // tests
        taxRateRepository.deleteAll();
        final TaxRate taxRate = new TaxRate();
        taxRate.setRate( 0.02 );
        taxRateRepository.save( taxRate );
    }

    /*
     * Tests setRate
     */
    @Test
    void testSetTaxRate () {
        taxRateService.setTaxRate( 0.05 );

        assertEquals( 0.05, taxRateRepository.findAll().get( 0 ).getRate() );
    }

    /*
     * Tests getRate
     */
    @Test
    void testGetTaxRate () {
        assertEquals( 0.02, taxRateService.getTaxRate() );

        taxRateService.setTaxRate( 0.05 );

        assertEquals( 0.05, taxRateService.getTaxRate() );
    }

    /*
     * Error Tests setRate. Tries to set a negative number as the rate
     */
    @Test
    void testSetTaxRateBelowZero () {
        final IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> {
            taxRateService.setTaxRate( -0.01 );
        } );
        assertEquals( "Invalid tax rate provided: Tax rate must be between 0 and 1.", exception.getMessage() );
    }

    /*
     * Error Tests setRate. Tries to set a number over 1 as the rate
     */
    @Test
    void testSetTaxRateAboveOne () {
        final IllegalArgumentException exception = assertThrows( IllegalArgumentException.class, () -> {
            taxRateService.setTaxRate( 1.01 );
        } );
        assertEquals( "Invalid tax rate provided: Tax rate must be between 0 and 1.", exception.getMessage() );
    }

    /*
     * Error Tests setRate. Tries to set a the rate when there is no rate in the
     * system
     */
    @Test
    void testSetTaxRateWithNoExistingRate () {
        taxRateRepository.deleteAll(); // Simulate no tax rate in the database
        final IllegalStateException exception = assertThrows( IllegalStateException.class, () -> {
            taxRateService.setTaxRate( 0.05 );
        } );
        assertEquals( "No tax rate exists in the system to update.", exception.getMessage() );
    }

    /*
     * Error Tests getRate. Tries to get the rate when there is no rate in the
     * system
     */
    @Test
    void testGetTaxRateWithNoExistingRate () {
        taxRateRepository.deleteAll(); // Simulate no tax rate in the database
        final IllegalStateException exception = assertThrows( IllegalStateException.class, () -> {
            taxRateService.getTaxRate();
        } );
        assertEquals( "Failed to retrieve tax rate: No tax rate exists in the system.", exception.getMessage() );
    }
}
