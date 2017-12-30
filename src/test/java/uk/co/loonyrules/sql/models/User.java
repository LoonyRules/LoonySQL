package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Primary;
import uk.co.loonyrules.sql.annotations.Table;

import java.util.UUID;

@Table(name = "users")
public class User
{

    @Column
    @Primary
    private int id;

    @Column
    private UUID uuid;

    @Column
    private String lastName;

    @Column
    private int random;

    @Column(maxLength = 1)
    private boolean banned;

    public User()
    {

    }

    public User(UUID uuid)
    {
        this.uuid = uuid;
    }

    public int getId()
    {
        return id;
    }

    public UUID getUUID()
    {
        return uuid;
    }

    public String getLastName()
    {
        return lastName;
    }

    public int getRandom()
    {
        return random;
    }

    public boolean isBanned()
    {
        return banned;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public void setRandom(int random)
    {
        this.random = random;
    }

    public void setBanned(boolean banned)
    {
        this.banned = banned;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", lastName='" + lastName + '\'' +
                ", random=" + random +
                ", banned=" + banned +
                '}';
    }
}