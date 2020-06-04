package no.unit.nva.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import no.unit.nva.database.UserDb;
import no.unit.nva.model.RoleDto.Builder;
import nva.commons.utils.JacocoGenerated;

public class UserDto {

    private String username;

    private String institution;

    public List<RoleDto> roles;


    public static UserDto fromUserDb(UserDb userDb){
        UserDto userDto= new UserDto();
        userDto.setUsername(userDb.getUsername());
        List<RoleDto> userRoles = Arrays.stream(userDb.getRoles().split(UserDb.ROLE_DELIMITER))
            .map(role -> new Builder().withName(role).build())
            .collect(Collectors.toList());

        userDto.setRoles(userRoles);
        return userDto;
    }


    public UserDb toUserDb(){
        UserDb userDb= new UserDb();
        userDb.setId(this.username);
        userDb.setUsername(this.username);
        String roleString= mapRolesToString();
        userDb.setRoles(roleString);
        return userDb;
    }

    private String mapRolesToString() {
        return this.roles.stream().map(RoleDto::getName).collect(Collectors.joining(UserDb.ROLE_DELIMITER));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
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
        return getUsername().equals(userDto.getUsername()) &&
            getInstitution().equals(userDto.getInstitution()) &&
            getRoles().equals(userDto.getRoles());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getUsername(), getInstitution(), getRoles());
    }
}
