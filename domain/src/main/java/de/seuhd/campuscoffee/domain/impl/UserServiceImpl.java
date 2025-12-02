package de.seuhd.campuscoffee.domain.impl;

import de.seuhd.campuscoffee.domain.exceptions.DuplicationException;
import de.seuhd.campuscoffee.domain.exceptions.MissingFieldException;
import de.seuhd.campuscoffee.domain.model.*;
import de.seuhd.campuscoffee.domain.ports.UserDataService;
import de.seuhd.campuscoffee.domain.ports.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    // TODO: Implement user service
    private final UserDataService userDataService;

    @Override
    public void clear() {
        log.warn("Clearing all User data");
        userDataService.clear();
    }

    @Override
    public @NonNull List<User> getAll() {
        log.debug("Retrieving all Users");
        return UserDataService.getAll();
    }

    @Override
    public @NonNull User getById(@NonNull Long id) {
        log.debug("Retrieving User with ID: {}", id);
        return userDataService.getById(id);
    }

    @Override
    public @NonNull Pos getByName(@NonNull String name) {
        log.debug("Retrieving User with name: {}", name);
        return userDataService.getByName(name);
    }

    @Override
    public @NonNull User upsert(@NonNull User user) {
        if (user.id() == null) {
            // create a new user
            log.info("Creating new User: {}", user.name());
        } else {
            // update an existing User
            log.info("Updating User with ID: {}", user.id());
            // user ID must be set
            Objects.requireNonNull(user.id());
            // User must exist in the database before the update
            userDataService.getById(user.id());
        }
        return performUpsert(user);
    }


    @Override
    public void delete(@NonNull Long id) {
        log.info("Trying to delete POS with ID: {}", id);
        posDataService.delete(id);
        log.info("Deleted POS with ID: {}", id);
    }


    /**
     * Performs the actual upsert operation with consistent error handling and logging.
     * Database constraint enforces name uniqueness - data layer will throw DuplicateEntityException if violated.
     * JPA lifecycle callbacks (@PrePersist/@PreUpdate) set timestamps automatically.
     *
     * @param User the User to upsert
     * @return the persisted User with updated ID and timestamps
     * @throws DuplicationException if a User with the same name already exists
     */
    private @NonNull User performUpsert(@NonNull User user) {
        try {
            User upsertedUser = userDataService.upsert(user);
            log.info("Successfully upserted User with ID: {}", upsertedUser.id());
            return upsertedUser;
        } catch (DuplicationException e) {
            log.error("Error upserting User '{}': {}", user.name(), e.getMessage());
            throw e;
        }
    }

}
