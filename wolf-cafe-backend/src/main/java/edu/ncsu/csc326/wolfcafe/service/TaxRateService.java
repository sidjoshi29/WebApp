
package edu.ncsu.csc326.wolfcafe.service;

/**
 * Tax Rate service
 */
public interface TaxRateService {
    /**
     * Sets the Tax Rate of the System
     *
     * @param rate
     *            new rate to set
     * @return whether the change was successful
     */
    boolean setTaxRate ( double rate );

    /**
     * Gets the current tax rate of the system
     *
     * @return the current tax rate of the system
     */
    double getTaxRate ();

}
