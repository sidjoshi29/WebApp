package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * Implemented AuthService
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final ModelMapper modelMapper;

	/**
	 * Registers the given user
	 *
	 * @param registerDto new user information
	 * @return message for success or failure
	 */
	@Override
	public String register(final RegisterDto registerDto) {
		// Check for duplicates - username
		if (userRepository.existsByUsername(registerDto.getUsername())) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
					"Username '" + registerDto.getUsername() + "' already exists.");
		}
		// Check for duplicates - email
		if (userRepository.existsByEmail(registerDto.getEmail())) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
					"Email '" + registerDto.getEmail() + "' already exists.");
		}

		final Role userRole = roleRepository.findByName("ROLE_CUSTOMER");
		if (userRole == null) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Role 'ROLE_CUSTOMER' not found in the system.");
		}

		// Create and save user
		final User user = new User();
		user.setName(registerDto.getName());
		user.setUsername(registerDto.getUsername());
		user.setEmail(registerDto.getEmail());
		user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

		final Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);

		try {
			userRepository.save(user);
			return "User registered successfully.";
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to save the user: " + ex.getMessage());
		}
	}

	/**
	 * Logins in the given user
	 *
	 * @param loginDto username/email and password
	 * @return response with authenticated user
	 */
	@Override
	public JwtAuthResponse login(final LoginDto loginDto) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String token = jwtTokenProvider.generateToken(authentication);
			User user = userRepository
					.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail())
					.orElseThrow(() -> new ResourceNotFoundException("User not found with provided credentials."));

			String role = user.getRoles().stream().findFirst().map(Role::getName)
					.orElseThrow(() -> new ResourceNotFoundException("User role not found."));

			JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
			jwtAuthResponse.setRole(role);
			jwtAuthResponse.setAccessToken(token);

			return jwtAuthResponse;
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password.");
		}
	}

	/**
	 * Deletes the given user by id
	 *
	 * @param id id of user to delete
	 */
	@Override
	public void deleteUserById(final Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

		try {
			userRepository.delete(user);
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to delete user: " + ex.getMessage());
		}
	}

	/**
	 * Edits the given user
	 *
	 * @param id          id of user
	 * @param registerDto the updated user fields
	 */
	@Override
	public String editUser(final Long id, final RegisterDto registerDto) {
		// Find the user by ID or throw an exception if not found
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

		// Validate fields from registerDto
		if (registerDto.getName() == null || registerDto.getName().trim().isEmpty() || registerDto.getUsername() == null
				|| registerDto.getUsername().trim().isEmpty() || registerDto.getEmail() == null
				|| registerDto.getEmail().trim().isEmpty() || registerDto.getPassword() == null
				|| registerDto.getPassword().trim().isEmpty()) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
					"All fields (name, username, email, password) are required.");
		}

		// Update user details
		user.setName(registerDto.getName());
		user.setUsername(registerDto.getUsername());
		user.setEmail(registerDto.getEmail());
		user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

		// Save the updated user
		try {
			userRepository.save(user);
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to update user: " + ex.getMessage());
		}

		return "User edited successfully";
	}

	/**
	 * Deletes the given user by id
	 *
	 * @param id id of user to delete
	 */
	@Override
	@Transactional
	public String createStaff(final RegisterDto registerDto) {
		if (userRepository.existsByUsername(registerDto.getUsername())
				|| userRepository.existsByEmail(registerDto.getEmail())) {
			throw new WolfCafeAPIException(HttpStatus.BAD_REQUEST,
					"A user with the provided username or email already exists.");
		}

		final Role staffRole = roleRepository.findByName("ROLE_STAFF");
		if (staffRole == null) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Role 'ROLE_STAFF' not found in the system.");
		}

		// Create and save staff user
		User staff = modelMapper.map(registerDto, User.class);
		staff.setPassword(passwordEncoder.encode(registerDto.getPassword()));
		staff.setRoles(Set.of(staffRole));

		try {
			userRepository.save(staff);
			return "Staff added successfully.";
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add staff: " + ex.getMessage());
		}
	}

	/**
	 * Returns all items
	 *
	 * @return all items
	 */
	@Override
	public List<User> getAllUsers() {
		try {
			return userRepository.findAll();
		} catch (Exception ex) {
			throw new WolfCafeAPIException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to retrieve users: " + ex.getMessage());
		}
	}
}
