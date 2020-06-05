package no.unit.nva.model;

import static nva.commons.utils.attempt.Try.attempt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.database.RoleDb;
import no.unit.nva.database.UserDb;
import no.unit.nva.database.exceptions.InvalidUserException;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.StringUtils;
import nva.commons.utils.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDto {

    public static final String MISSING_FIELD_ERROR = "Invalid User. Missing obligatory field: ";
    public static final String ERROR_DUE_TO_INVALID_ROLE =
        "Failure while trying to create user with role without rolename";
    private String username;

    private String institution;

    public List<RoleDto> roles;

    private static final Logger logger = LoggerFactory.getLogger(UserDto.class);

    public UserDto() {
    }

    private UserDto(Builder builder) {
        setUsername(builder.username);
        setInstitution(builder.institution);
        setRoles(builder.roles);
    }

    public static UserDto fromUserDb(UserDb userDb) throws InvalidUserException {

        UserDto.Builder userDto = new UserDto.Builder();
        userDto
            .withUsername(userDb.getUsername())
            .withRoles(extractRoles(userDb))
            .withInstitution(userDb.getInstitution());
        return userDto.build();
    }

    private static List<RoleDto> extractRoles(UserDb userDb) {
        return Optional.ofNullable(userDb)
            .stream()
            .flatMap(userDb1 -> userDb1.getRoles().stream())
            .map(attempt(RoleDto::fromRoleDb))
            .map(eff -> eff.orElseThrow(UserDto::unexpectedException))
            .collect(Collectors.toList());
    }

    /*This exception should not happen as a RoleDb  should always convert to a RoleDto */
    private static <T> IllegalStateException unexpectedException(Failure<T> failure) {
        logger.error(ERROR_DUE_TO_INVALID_ROLE);
        throw new IllegalStateException(failure.getException());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public UserDb toUserDb() throws InvalidUserException {
        UserDb.Builder userDb = UserDb.newBuilder()
            .withUsername(username)
            .withInstitution(institution)
            .withRoles(createRoleDb());

        return userDb.build();
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public String getInstitution() {
        return institution;
    }

    private void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    private void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }

    private List<RoleDb> createRoleDb() {
        return
            Optional.ofNullable(this.roles)
                .stream()
                .flatMap(Collection::stream)
                .map(attempt(RoleDto::toRoleDb))
                .map(eff -> eff.orElseThrow(UserDto::unexpectedException))
                .collect(Collectors.toList());
    }



    public Builder copy() {
        return new Builder()
            .withUsername(username)
            .withInstitution(institution)
            .withRoles(new ArrayList<>(roles));
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserDto userDto = (UserDto) o;
        return Objects.equals(getUsername(), userDto.getUsername())
            && Objects.equals(getInstitution(), userDto.getInstitution())
            && Objects.equals(getRoles(), userDto.getRoles());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getUsername(), getInstitution(), getRoles());
    }

    public static final class Builder {

        private String username;
        private String institution;
        private List<RoleDto> roles;

        private Builder() {
            roles = Collections.emptyList();
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withInstitution(String institution) {
            this.institution = institution;
            return this;
        }

        public Builder withRoles(List<RoleDto> roles) {
            this.roles = roles;
            return this;
        }

        public UserDto build() throws InvalidUserException {
            if (StringUtils.isEmpty(username)) {
                throw new InvalidUserException(MISSING_FIELD_ERROR + "username");
            }
            return new UserDto(this);
        }
    }
}
