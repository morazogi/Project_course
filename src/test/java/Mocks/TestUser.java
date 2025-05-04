package DomainLayer;

public class TestUser extends User {

    public TestUser() {
        super(); // optional, but explicit
        this.myToken = "test-token"; // optional — can be used in tests if needed
    }

    @Override
    public String toString() {
        return "TestUser{id=" + getID() + "}";
    }
}
