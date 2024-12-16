package edu.ncsu.csc326.wolfcafe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.InventoryDto;
import edu.ncsu.csc326.wolfcafe.service.InventoryService;

/**
 * Controller for WolfCafe's inventory.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "api/inventory" )
public class InventoryController {

    /** Connection to inventory service for manipulating the Inventory model. */
    @Autowired
    private InventoryService inventoryService;

    /**
     * REST API endpoint to provide GET access to a WolfCafe Inventory. Requires
     * the STAFF role
     *
     * @return response to the request
     */
    @GetMapping
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<InventoryDto> getInventory () {
        // Directly call the service; exceptions are handled globally
        final InventoryDto inventoryDto = inventoryService.getInventory();
        return ResponseEntity.ok( inventoryDto );
    }

    /**
     * REST API endpoint to provide update access to a WolfCafe Inventory.
     * Requires the STAFF role
     *
     * @param inventoryDto
     *            amounts to update in the inventory
     * @return response to the request
     */
    @PutMapping
    @PreAuthorize ( "hasRole('STAFF')" )
    public ResponseEntity<InventoryDto> updateInventory ( @RequestBody final InventoryDto inventoryDto ) {
        // Directly call the service; exceptions are handled globally
        final InventoryDto savedInventoryDto = inventoryService.updateInventory( inventoryDto );
        return ResponseEntity.ok( savedInventoryDto );
    }
}
