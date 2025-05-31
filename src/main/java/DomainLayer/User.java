package DomainLayer;

import DomainLayer.Roles.Guest;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class User extends Guest{

    public User() {
    }

}