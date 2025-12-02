package de.seuhd.campuscoffee.api.controller;

import de.seuhd.campuscoffee.api.dtos.PosDto;
import de.seuhd.campuscoffee.api.dtos.UserDto;
import de.seuhd.campuscoffee.api.exceptions.ErrorResponse;
import de.seuhd.campuscoffee.api.mapper.UserDtoMapper;
import de.seuhd.campuscoffee.domain.model.CampusType;
import de.seuhd.campuscoffee.domain.ports.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static de.seuhd.campuscoffee.api.util.ControllerUtils.getLocation;

@Tag(name = "Users", description = "Operations related to user management.")
@Controller
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    //TODO: Implement user controller
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    @operation(
            summary = "get all Users",
            response = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "array", implementation = UserDto.class)
                            ),
                            description = "All Users as a JSON array."
                    )
            }
    )
    @GetMapping("/AllUsers")
    public ResponseEntity<List<UserDto>> getAll() {

        return ResponseEntity.ok(
                userService.getAll().stream()
                        .map(userDtoMapper::fromDomain)
                        .toList()
        );
    }

    @Operation(
            summary = "Get User by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            ),
                            description = "The User with the provided ID as a JSON object."
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "No User with the provided ID could be found."
                    )
            }
    )
    @GetMapping("/user/{id}")
    public ResponseEntity<PosDto> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                posDtoMapper.fromDomain(posService.getById(id))
        );
    }

    @Operation(
            summary = "Get User by name.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            ),
                            description = "The User with the provided name as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "No User with the provided name could be found."
                    )
            }
    )
    @GetMapping("/user/filter")
    public ResponseEntity<UserDto> filter(
            @RequestParam("name") String name) {

        return ResponseEntity.ok(
                userDtoMapper.fromDomain(userService.getByName(name))
        );
    }

    @Operation(
            summary = "Create a new User.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PosDto.class)
                            ),
                            description = "The new User as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "Validation failed (e.g., invalid address, postal code, or bean validation errors)."
                    )
            }
    )
    @PostMapping("/user")
    public ResponseEntity<UserDto> create(
            @RequestBody @Valid UserDto userDto) {

        UserDto created = upsert(UserDto);
        return ResponseEntity
                .created(getLocation(created.id()))
                .body(created);
    }
    @PostMapping("/import/osm/{nodeId}")
    public ResponseEntity<PosDto> create(
            @PathVariable Long nodeId,
            @RequestBody CampusType campusType) {

        PosDto createdPos = posDtoMapper.fromDomain(
                posService.importFromOsmNode(nodeId, campusType)
        );
        return ResponseEntity
                .created(getLocation(createdPos.id()))
                .body(createdPos);
    }

    @Operation(
            summary = "Update an existing User by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserDto.class)
                            ),
                            description = "The updated User as a JSON object."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "Validation failed: IDs in path and body do not match, invalid address, postal code, or bean validation errors."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "No User with the provided ID could be found."
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Long id,
            @RequestBody @Valid UserDto userDto) {

        if (!id.equals(userDto.id())) {
            throw new IllegalArgumentException("User ID in path and body do not match.");
        }
        return ResponseEntity.ok(
                upsert(userDto)
        );
    }

    @Operation(
            summary = "Delete a User by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "The User was successfully deleted."
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            ),
                            description = "No User with the provided ID could be found."
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        userService.delete(id); // throws NotFoundException if no POS with the provided ID exists
        return ResponseEntity.noContent().build();
    }

    /**
     * Common upsert logic for create and update.
     *
     * @param userDto the POS DTO to map and upsert
     * @return the upserted POS mapped back to the DTO format.
     */
    private UserDto upsert(UserDto userDto) {
        return userDtoMapper.fromDomain(
                userService.upsert(
                        userDtoMapper.toDomain(userDto)
                )
        );
    }
            }
    )
}
