package Mocks;

import DomainLayer.Roles.RegisteredUser;

import java.util.LinkedList;
import java.util.List;

/**
 * A test implementation of RegisteredUser that can be used in tests
 * instead of mocking RegisteredUser, which has issues with Mockito.
 * 
 * This class is part of a group of test implementation classes:
 * - TestRegisteredUser
 * - TestOwnership
 * - TestManaging
 * - TestStore
 * 
 * See README.md in this directory for more details on why these
 * test implementations are used instead of mocking or real implementations.
 */
public class TestRegisteredUser extends RegisteredUser {
    private String testId;

    public TestRegisteredUser(String id) {
        super("TestUser");
        this.testId = id;
    }

    @Override
    public String getID() {
        return testId;
    }

}