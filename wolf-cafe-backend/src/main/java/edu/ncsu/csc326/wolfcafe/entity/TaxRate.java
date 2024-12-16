package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing the tax rate of the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "tax_rate" )
public class TaxRate {
    /** Unique identifier for the tax rate in the system. */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    @Positive ( message = "Tax Rate must be positive." )
    private Long   id;
    /** The rate of the tax */
    private Double rate;

}
