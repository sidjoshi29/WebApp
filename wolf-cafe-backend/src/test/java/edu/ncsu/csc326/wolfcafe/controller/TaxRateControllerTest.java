package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.service.TaxRateService;

/*
 * Tests TaxRateController
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaxRateControllerTest {

    /*
     * The admin oassword
     */
    @Value ( "${app.admin-user-password}" )
    private String adminUserPassword;

    /*
     * Reference to the MVC for HTTP requests
     */
    @Autowired
    private MockMvc mvc;

    /*
     * Reference to tax rate service
     */
    private TaxRateService taxRateService;

    /*
     * Reference to tax rate repository
     */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /*
     * Sets the tax rate to the default value before next test
     */
    @BeforeEach
    void setUp () throws Exception {
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
     * Helper method to get the Admin Token to carry out ADMIN only requests.
     * @return the admin token
     */
    private String getAdminToken () throws Exception {
        final LoginDto loginDto = new LoginDto( "admin", adminUserPassword );

        final MvcResult result = mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( new ObjectMapper().writeValueAsString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andReturn();

        final String responseContent = result.getResponse().getContentAsString();

        final JsonNode jsonNode = new ObjectMapper().readTree( responseContent );

        return jsonNode.get( "accessToken" ).asText();
    }

    /*
     * Tests getRate
     */
    @Test
    @Transactional
    void testGetTaxRate () throws Exception {
        final String token = getAdminToken();

        mvc.perform( get( "/api/taxRate" ).header( "Authorization", "Bearer " + token )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.rate" ).value( 0.02 ) );
    }

    /*
     * Tests setRate
     */
    @Test
    @Transactional
    void testSetTaxRate () throws Exception {

        final String token = getAdminToken();

        mvc.perform( put( "/api/taxRate" ).header( "Authorization", "Bearer " + token )
                .contentType( MediaType.APPLICATION_JSON ).content( "0.05" ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.success" ).value( true ) ); // Validate

        mvc.perform( get( "/api/taxRate" ).header( "Authorization", "Bearer " + token )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.rate" ).value( 0.05 ) );

    }

}
