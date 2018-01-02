package uk.co.loonyrules.sql.models;

import uk.co.loonyrules.sql.annotations.Column;
import uk.co.loonyrules.sql.annotations.Primary;
import uk.co.loonyrules.sql.annotations.Table;
import uk.co.loonyrules.sql.enums.ModifyType;
import uk.co.loonyrules.sql.enums.Rank;

import java.util.UUID;

@Table(name = "users", modifyType = ModifyType.NONE)
public class User
{

    @Column
    @Primary
    private int id = 0;

    @Column
    private UUID uuid;

    @Column
    private String lastName;

    @Column
    private int random;

    @Column
    private boolean banned;

    @Column
    private Rank rank = Rank.DEFAULT;

    public User()
    {

    }

    public User(UUID uuid)
    {
        this.uuid = uuid;
    }

    public User(UUID uuid, String lastName)
    {
        this.uuid = uuid;
        this.lastName = lastName;
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

    public Rank getRank()
    {
        return rank;
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

    public void setRank(Rank rank)
    {
        this.rank = rank;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", uuid=" + uuid +
                ", lastName='" + lastName + '\'' +
                ", random=" + random +
                ", banned=" + banned +
                ", rank=" + rank.toString() +
                '}';
    }

}