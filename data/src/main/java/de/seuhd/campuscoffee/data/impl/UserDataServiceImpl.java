package de.seuhd.campuscoffee.data.impl;

import de.seuhd.campuscoffee.domain.ports.UserDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the user data service that the domain layer provides as a port.
 * This layer is responsible for data access and persistence.
 * Business logic should be in the service layer.
 */
@Service
@RequiredArgsConstructor
class UserDataServiceImpl implements UserDataService {

    //TODO: Uncomment after user domain object is defined and add imports

    @Override
    public void clear() {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;
        userRepository.deleteAllInBatch();
        userRepository.flush();
        userRepository.resetSequence(); // ensure consistent IDs after clearing (for local testing)
    }

    @Override
    @NonNull
    public List<User> getAll() {
        return userRepository.findAll().stream()
                .map(userEntityMapper::fromEntity)
                .toList();
    }

    @Override
    @NonNull
    public User getById(@NonNull Long id) {
        return userRepository.findById(id)
                .map(userEntityMapper::fromEntity)
                .orElseThrow(() -> new NotFoundException(User.class, id));
    }

    @Override
    @NonNull
    public User getByLoginName(@NonNull String loginName) {
        return userRepository.findByLoginName(loginName)
                .map(userEntityMapper::fromEntity)
                .orElseThrow(() -> new NotFoundException(User.class, UserEntity.LOGIN_NAME_COLUMN, loginName));
    }

    @Override
    @NonNull
    public User upsert(@NonNull User user) {
        // map User domain object to entity and save
        try {
            if (user.id() == null) {
                // Create a new user
                return userEntityMapper.fromEntity(
                        userRepository.saveAndFlush(userEntityMapper.toEntity(user))
                );
            }

            // update an existing user
            UserEntity userEntity = userRepository.findById(user.id())
                    .orElseThrow(() -> new NotFoundException(User.class, user.id()));

            // update entity with data from domain model
            userEntityMapper.updateEntity(user, userEntity);

            return userEntityMapper.fromEntity(userRepository.saveAndFlush(userEntity));
        } catch (DataIntegrityViolationException e) {
            // translate database constraint violations to domain exceptions
            // this is the adapter's responsibility in hexagonal architecture
            if (ConstraintViolationChecker.isConstraintViolation(e, UserEntity.LOGIN_NAME_CONSTRAINT)) {
                throw new DuplicationException(User.class, UserEntity.LOGIN_NAME_COLUMN, user.loginName());
            } else if (ConstraintViolationChecker.isConstraintViolation(e, UserEntity.EMAIL_ADDRESS_CONSTRAINT)) {
                throw new DuplicationException(User.class, UserEntity.EMAIL_ADDRESS_COLUMN, user.emailAddress());
            }
            // re-throw if it's a different constraint violation
            throw e;
        }
    }

    @Override
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(User.class, id);
        }
        userRepository.deleteById(id);
    }
}
