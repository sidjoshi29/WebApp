package edu.ncsu.csc326.wolfcafe.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import jakarta.persistence.EntityManager;

/*
 * Tests the Tax Rate Repository
 */
@DataJpaTest
@AutoConfigureTestDatabase ( replace = Replace.NONE )
class TaxRateRepositoryTest {

    /**
     * Reference to the tax rate repository
     */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /**
     * Reference to the entity manager
     */
    @Autowired
    private TestEntityManager testEntityManager;

    /*
     * Sets up the tests so that the default tax rate is set back to 2.0%
     */
    @BeforeEach
    void setUp () throws Exception {

        final EntityManager entityManager = testEntityManager.getEntityManager();

        // Reset the tax rate to the default value for tests

        // This mimics the same code in the Config file

        final double defaultTaxRate = 0.02;
        final TaxRate taxRate;

        if ( taxRateRepository.findAll().isEmpty() ) {
            taxRate = new TaxRate();
        }
        else {
            taxRate = taxRateRepository.findAll().get( 0 );
        }

        taxRate.setRate( defaultTaxRate );
        taxRateRepository.save( taxRate );

    }

    /*
     * Tests getRate
     */
    @Test
    void testGetTaxRate () {
        final List<TaxRate> rateList = taxRateRepository.findAll();
        assertEquals( 1, rateList.size() );
        assertEquals( 0.02, rateList.getFirst().getRate() );
    }

    /*
     * Tests setRate
     */
    @Test
    void testSetTaxRate () {
        final List<TaxRate> rateList = taxRateRepository.findAll();
        assertEquals( 1, rateList.size() );
        rateList.getFirst().setRate( 0.01 );
        assertEquals( 0.01, rateList.getFirst().getRate() );
    }

}
