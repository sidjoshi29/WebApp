package edu.ncsu.csc326.wolfcafe.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.TaxRate;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;

/**
 * Sets up the database with roles and a default admin user. Based on code from
 * https://github.com/Baeldung/spring-security-registration/blob/master/src/main/java/com/baeldung/spring/SetupDataLoader.java
 */
@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    /** True if already setup */
    private boolean           alreadySetup = false;

    /** Link to RoleRepository */
    @Autowired
    private RoleRepository    roleRepository;

    /** Link to UserRepository */
    @Autowired
    private UserRepository    userRepository;

    /** Link to TaxRateRepository */
    @Autowired
    private TaxRateRepository taxRateRepository;

    /** Encodes passwords */
    @Autowired
    private PasswordEncoder   passwordEncoder;

    /** Admin password in application.properties file */
    @Value ( "${app.admin-user-password}" )
    private String            adminUserPassword;

    /**
     * When the application loads and the context is refreshed this method will
     * run and create the admin user role and any other user roles defined in
     * the Roles. UserRoles enum. The tax rate of the system is also reset to
     * 2.0%
     */
    @Override
    @Transactional
    public void onApplicationEvent ( final ContextRefreshedEvent event ) {
        if ( alreadySetup ) {
            return;
        }

        final Role adminRole = createRoleIfNotFound( Roles.ROLE_ADMIN );
        for ( final Roles.UserRoles role : Roles.UserRoles.values() ) {
            createRoleIfNotFound( role.toString() );
        }

        createUserIfNotFound( "Admin User", "admin", "admin@admin.edu", new ArrayList<>( Arrays.asList( adminRole ) ) );

        if ( taxRateRepository.findAll().isEmpty() ) {
            createRateIfNotFound();
        }

        alreadySetup = true;

    }

    /**
     * Creates the role with the given name.
     *
     * @param name
     *            role name
     * @return created role
     */
    @Transactional
    public Role createRoleIfNotFound ( final String name ) {
        Role role = roleRepository.findByName( name );
        if ( role == null ) {
            role = new Role();
            role.setName( name );
        }
        role = roleRepository.save( role );
        return role;

    }

    /**
     * Creates the tax rate of the system (to 2.0 %) if it already isn't set
     *
     * @return the created tax rate for the system
     */
    @Transactional
    public TaxRate createRateIfNotFound () {
        final TaxRate rate = new TaxRate();
        rate.setRate( 0.02 );

        taxRateRepository.save( rate );

        return rate;
    }

    /**
     * Creates a user with the given information
     *
     * @param name
     *            user's name
     * @param username
     *            user's username
     * @param email
     *            user's email
     * @param roles
     *            user's roles
     * @return created user
     */
    @Transactional
    public User createUserIfNotFound ( final String name, final String username, final String email,
            final Collection<Role> roles ) {
        final Optional<User> returnedUser = userRepository.findByUsernameOrEmail( username, email );

        if ( returnedUser.isEmpty() ) {
            final User user = new User();
            user.setName( name );
            user.setUsername( username );
            user.setEmail( email );
            user.setPassword( passwordEncoder.encode( adminUserPassword ) );
            user.setRoles( roles );
            userRepository.save( user );
            return user;
        }
        else {
            return returnedUser.get();
        }

    }

}
