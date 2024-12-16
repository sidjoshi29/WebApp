package edu.ncsu.csc326.wolfcafe.entity;

import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing a user in the WolfCafe system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table ( name = "users" )
public class User {
    /** Unique identifier for a user in the system. */
    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long             id;
    /** Name of the user */
    private String           name;
    /** Username of the user */
    @Column ( nullable = false, unique = true )
    private String           username;
    /** Email of the user */
    @Column ( nullable = false, unique = true )
    private String           email;
    /** Password of the user */
    @Column ( nullable = false )
    private String           password;
    /** The specific role that the user possesses (STAFF, CUSTOMER, ADMIN) */
    @ManyToMany ( fetch = FetchType.EAGER )
    @JoinTable ( name = "users_roles", joinColumns = @JoinColumn ( name = "user_id", referencedColumnName = "id" ),
            inverseJoinColumns = @JoinColumn ( name = "role_id", referencedColumnName = "id" ) )
    private Collection<Role> roles;

}
