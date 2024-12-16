package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a user role in the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "roles" )
public class Role {
    /** Unique identifier for a user role. */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long   id;
    /** The name of the user role */
    private String name;

}
