/**
 *
 */
package edu.ncsu.csc326.wolfcafe.entity;

/**
 * Enum representing the various statuses of an order.
 */
public enum OrderStatus {
	PLACED, // Order has been created
	FULFILLED, // Order is prepared and ready for pickup/delivery
	PICKED_UP, // Order has been picked up/delivered
	CANCELLED // Order has been canceled
}
