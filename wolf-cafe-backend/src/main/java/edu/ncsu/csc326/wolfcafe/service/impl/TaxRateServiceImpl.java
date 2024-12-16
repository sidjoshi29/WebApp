package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;
import lombok.AllArgsConstructor;

/**
 * Implementation of the TaxRateService Interface
 */
@Service
@AllArgsConstructor
public class TaxRateServiceImpl implements TaxRateService {

    /** Connection to the tax rate repository */
    private final TaxRateRepository taxRateRepository;

    /**
     * Sets the Tax Rate of the System
     *
     * @param rate
     *            new rate to set
     * @return whether the change was successful
     */
    @Override
    public boolean setTaxRate ( final double rate ) {
        try {
            if ( rate < 0 || rate > 1 ) {
                throw new IllegalArgumentException( "Tax rate must be between 0 and 1." );
            }

            final List<TaxRate> rateList = taxRateRepository.findAll();

            if ( rateList.isEmpty() ) {
                throw new IllegalStateException( "No tax rate exists in the system to update." );
            }

            final TaxRate systemRate = rateList.get( 0 );
            systemRate.setRate( rate );
            taxRateRepository.save( systemRate );

            return true;
        }
        catch ( final IllegalArgumentException e ) {
            throw new IllegalArgumentException( "Invalid tax rate provided: " + e.getMessage() );
        }
        catch ( final IllegalStateException e ) {
            throw e;
        }
        catch ( final Exception e ) {
            throw new RuntimeException( "An unexpected error occurred while setting the tax rate: " + e.getMessage() );
        }
    }

    /**
     * Gets the current tax rate of the system
     *
     * @return the current tax rate of the system
     */
    @Override
    public double getTaxRate () {
        try {
            final List<TaxRate> rateList = taxRateRepository.findAll();

            if ( rateList.isEmpty() ) {
                throw new IllegalStateException( "No tax rate exists in the system." );
            }

            return rateList.get( 0 ).getRate();
        }
        catch ( final IllegalStateException e ) {
            throw new IllegalStateException( "Failed to retrieve tax rate: " + e.getMessage() );
        }
        catch ( final Exception e ) {
            throw new RuntimeException(
                    "An unexpected error occurred while retrieving the tax rate: " + e.getMessage() );
        }
    }
}
