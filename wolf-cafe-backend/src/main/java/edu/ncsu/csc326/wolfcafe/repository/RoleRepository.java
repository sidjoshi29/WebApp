package edu.ncsu.csc326.wolfcafe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.ncsu.csc326.wolfcafe.entity.Role;

/**
 * Repository interface for Roles.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds the role with the given name.
     *
     * @param name
     *            name of the role needed
     * @return the Role associated with the given name
     */
    Role findByName ( String name );
}
