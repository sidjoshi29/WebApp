package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information for a tax rate in the system.
 */
/**
 * Data Transfer Object (DTO) for representing the information for the tax rate
 * of the WolfCafe system. The TaxRateDto holds the unique id and rate of the
 * tax rate in the system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateDto {
    /*
     * The unique id of the Tax Rate
     */
    private long id;
    /*
     * The rate of the tax
     */
    private double rate;

}
