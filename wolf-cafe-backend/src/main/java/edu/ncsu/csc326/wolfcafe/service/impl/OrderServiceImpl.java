package edu.ncsu.csc326.wolfcafe.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.OrderDto;
import edu.ncsu.csc326.wolfcafe.entity.Inventory;
import edu.ncsu.csc326.wolfcafe.entity.Item;
import edu.ncsu.csc326.wolfcafe.entity.Order;
import edu.ncsu.csc326.wolfcafe.entity.OrderStatus;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.InventoryRepository;
import edu.ncsu.csc326.wolfcafe.repository.ItemRepository;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.OrderService;

/**
 * Implementation of the Order Service interface.
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** Connection to the order repository */
    @Autowired
    private OrderRepository     orderRepository;

    /** Connection to the item repository */
    @Autowired
    private ItemRepository      itemRepository;

    /** Connection to the inventory repository */
    @Autowired
    private InventoryRepository inventoryRepository;

    /** Connection to the tax rate repository */
    @Autowired
    private TaxRateRepository   taxRateRepository;

    /** This is used to help map to order dtos */
    @Autowired
    private ModelMapper         modelMapper;

    /** Connection to user repository */
    @Autowired
    private UserRepository      userRepository;

    // private static final Logger logger = LoggerFactory.getLogger(
    // OrderServiceImpl.class );

    /**
     * Places a new order for the authenticated user. Verifies item availability
     * in inventory and reduces quantities accordingly. Sets the order status to
     * PLACED upon successful creation.
     *
     * @param orderDto
     *            Data Transfer Object containing order details
     * @return OrderDto containing the saved order details
     */
    // GEN AI used
    @Override
    @Transactional
    public OrderDto placeOrder ( final OrderDto orderDto ) throws ResourceNotFoundException, IllegalStateException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();

        final User user = userRepository.findByUsernameOrEmail( username, username )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found for username: " + username ) );

        final Order order = modelMapper.map( orderDto, Order.class );
        order.setCustomer( user );
        order.setStatus( OrderStatus.PLACED );
        order.setCreatedAt( LocalDateTime.now() );

        final Inventory inventory = inventoryRepository.findAll().stream().findFirst()
                .orElseThrow( () -> new ResourceNotFoundException( "Inventory is empty or not found." ) );

        double subtotal = 0.0;

        for ( final Map.Entry<String, Integer> entry : orderDto.getItems().entrySet() ) {
            final String itemName = entry.getKey();
            final int orderQuantity = entry.getValue();

            final Item item = itemRepository.findByName( itemName )
                    .orElseThrow( () -> new ResourceNotFoundException( "Item not found: " + itemName ) );

            final Integer inventoryQuantity = inventory.getItems().get( item );
            if ( inventoryQuantity == null || inventoryQuantity < orderQuantity ) {
                throw new IllegalStateException( "Insufficient inventory for item: " + itemName + ". Available: "
                        + ( inventoryQuantity != null ? inventoryQuantity : 0 ) + ", Required: " + orderQuantity );
            }

            inventory.getItems().put( item, inventoryQuantity - orderQuantity );
            order.getItems().put( itemName, orderQuantity );

            subtotal += item.getPrice() * orderQuantity;
        }

        // Fetch the tax rate from the repository
        final Optional<TaxRate> optionalTaxRate = taxRateRepository.findAll().stream().findFirst();
        final TaxRate taxRate = optionalTaxRate
                .orElseThrow( () -> new ResourceNotFoundException( "Tax rate not found." ) );
        final double rate = taxRate.getRate();

        final double tax = subtotal * rate;
        final double tip = orderDto.getTip() != null ? orderDto.getTip() : 0.0;

        order.setTotalPrice( subtotal + tax + tip );
        order.setTip( tip );

        inventoryRepository.save( inventory );
        final Order savedOrder = orderRepository.save( order );

        final OrderDto savedOrderDto = modelMapper.map( savedOrder, OrderDto.class );
        savedOrderDto.setCustomerId( user.getId() );
        return savedOrderDto;
    }

    /**
     * Fulfills an order if it is in PLACED status. Updates the order status to
     * FULFILLED.
     *
     * @param id
     *            ID of the order to fulfill
     * @return true if the order was successfully fulfilled, false otherwise
     */
    @Override
    @Transactional
    public boolean fulfillOrder ( final Long id ) throws ResourceNotFoundException {
        final Order order = findOrderById( id );

        if ( order.getStatus() == OrderStatus.PLACED ) {
            order.setStatus( OrderStatus.FULFILLED );
            orderRepository.save( order );
            return true;
        }

        throw new IllegalStateException( "Order with ID " + id + " is not in PLACED status and cannot be fulfilled." );
    }

    /**
     * Cancels an order if it is in PLACED status and returns items to
     * inventory. Throws WolfCafeAPIException if the order cannot be canceled
     * due to its current status.
     *
     * @param id
     *            ID of the order to cancel
     * @return true if the order was successfully canceled, false otherwise
     */
    @Override
    @Transactional
    public boolean cancelOrder ( final Long id ) throws ResourceNotFoundException, AccessDeniedException {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();

        userRepository.findByUsernameOrEmail( username, username )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found for username: " + username ) );

        final Order order = findOrderById( id );

        if ( order.getStatus() == OrderStatus.PLACED ) {
            final Inventory inventory = inventoryRepository.findAll().stream().findFirst()
                    .orElseThrow( () -> new ResourceNotFoundException( "Inventory is empty or not found." ) );

            for ( final Map.Entry<String, Integer> entry : order.getItems().entrySet() ) {
                final String itemName = entry.getKey();
                final int orderQuantity = entry.getValue();

                final Item item = itemRepository.findByName( itemName ).orElseThrow(
                        () -> new ResourceNotFoundException( "Item not found in inventory: " + itemName ) );

                inventory.getItems().put( item, inventory.getItems().getOrDefault( item, 0 ) + orderQuantity );
            }

            order.setStatus( OrderStatus.CANCELLED );
            inventoryRepository.save( inventory );
            orderRepository.save( order );
            return true;
        }

        throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                "Order with ID " + id + " cannot be canceled as it is not in PLACED status." );
    }

    /**
     * Retrieves all orders in the system and converts them to DTO format.
     *
     * @return List of all orders as OrderDto objects
     */
    @Override
    public List<OrderDto> getAllOrders () {
        final List<Order> orders = orderRepository.findAll();
        return orders.stream().map( order -> modelMapper.map( order, OrderDto.class ) ).collect( Collectors.toList() );
    }

    /**
     * Retrieves an order by its ID and converts it to DTO format.
     *
     * @param id
     *            ID of the order to retrieve
     * @return OrderDto containing the order details
     */
    @Override
    public OrderDto getOrder ( final Long id ) {
        final Order order = findOrderById( id );
        return modelMapper.map( order, OrderDto.class );
    }

    /**
     * Helper method to find an order by ID. Throws ResourceNotFoundException if
     * the order does not exist.
     *
     * @param id
     *            ID of the order to find
     * @return Order entity if found
     */
    private Order findOrderById ( final Long id ) {
        return orderRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "Order not found with id: " + id ) );
    }

    /**
     * Retrieves all orders for the currently authenticated user.
     *
     * @return List of OrderDto objects representing the user's orders
     */
    @Override
    public List<OrderDto> getOrdersForCurrentUser () {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();

        final User user = userRepository.findByUsernameOrEmail( username, username )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found" ) );

        final List<Order> orders = orderRepository.findByCustomerId( user.getId() );
        return orders.stream().map( order -> modelMapper.map( order, OrderDto.class ) ).collect( Collectors.toList() );
    }

    /**
     * Set the status of order to PICKED_UP
     *
     * @param id
     *            ID of the order that was picked up
     * @return true if the order was successfully picked up, or false upon error
     */
    @Override
    public boolean pickupOrder ( final Long id ) throws ResourceNotFoundException {
        final Order order = findOrderById( id );

        if ( order.getStatus() == OrderStatus.FULFILLED ) {
            order.setStatus( OrderStatus.PICKED_UP );
            orderRepository.save( order );
            return true;
        }

        throw new IllegalStateException(
                "Order with ID " + id + " is not in FULFILLED status and cannot be picked up." );
    }

    /**
     * Retrieves order history with optional filtering by item name.
     *
     * @param itemName
     *            (Optional) The name of the item to filter the orders by.
     * @return A list of OrderDto objects representing the order history.
     */
    @Override
    public List<OrderDto> viewOrderHistory ( final String itemName ) {
        List<Order> orders;

        if ( itemName == null || itemName.isEmpty() ) {
            // Fetch all orders if no item name is provided
            orders = orderRepository.findAll().stream().filter( order -> order.getStatus() == OrderStatus.PICKED_UP )
                    .collect( Collectors.toList() );
        }
        else {
            // Fetch orders containing the specified item
            orders = orderRepository.findAll().stream().filter(
                    order -> order.getStatus() == OrderStatus.PICKED_UP && order.getItems().containsKey( itemName ) )
                    .collect( Collectors.toList() );
        }

        // Map orders to DTOs
        return orders.stream().map( order -> modelMapper.map( order, OrderDto.class ) ).collect( Collectors.toList() );
    }

}
