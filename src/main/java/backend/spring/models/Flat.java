package backend.spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@Entity
public class Flat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nameFlat;
    private int price;
    private double square;
    private int storeys;
    private String wallingMaterial;
    private int rooms;
    private int floor;
    private String address;
    @Column(columnDefinition = "LONGBLOB")
    private ArrayList<Double> rating = new ArrayList<>();
    private Double averageRating;
    private int guests;
    @Column(columnDefinition = "LONGBLOB")
    private ArrayList<String> listPhotoFlats = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private User user;

    public Flat() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameFlat() {
        return nameFlat;
    }

    public void setNameFlat(String nameFlat) {
        this.nameFlat = nameFlat;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getSquare() {
        return square;
    }

    public void setSquare(double square) {
        this.square = square;
    }

    public int getStoreys() {
        return storeys;
    }

    public void setStoreys(int storeys) {
        this.storeys = storeys;
    }

    public String getWallingMaterial() {
        return wallingMaterial;
    }

    public void setWallingMaterial(String wallingMaterial) {
        this.wallingMaterial = wallingMaterial;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<String> getListPhotoFlats() {
        return listPhotoFlats;
    }

    public void setPhotoFlats(String PhotoFlat) {
        this.listPhotoFlats.add(PhotoFlat);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Double> getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating.add(rating);
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flat flat = (Flat) o;
        return id == flat.id &&
                price == flat.price &&
                Double.compare(flat.square, square) == 0 &&
                storeys == flat.storeys &&
                rooms == flat.rooms &&
                floor == flat.floor &&
                guests == flat.guests &&
                Objects.equals(nameFlat, flat.nameFlat) &&
                Objects.equals(wallingMaterial, flat.wallingMaterial) &&
                Objects.equals(address, flat.address) &&
                Objects.equals(rating, flat.rating) &&
                Objects.equals(averageRating, flat.averageRating) &&
                Objects.equals(listPhotoFlats, flat.listPhotoFlats) &&
                Objects.equals(user, flat.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nameFlat, price, square, storeys, wallingMaterial, rooms, floor, address, rating, averageRating, guests, listPhotoFlats, user);
    }

    @Override
    public String toString() {
        return "Flat{" +
                "id=" + id +
                ", nameFlat='" + nameFlat + '\'' +
                ", price=" + price +
                ", square=" + square +
                ", storeys=" + storeys +
                ", wallingMaterial='" + wallingMaterial + '\'' +
                ", rooms=" + rooms +
                ", floor=" + floor +
                ", address='" + address + '\'' +
                ", rating=" + rating +
                ", avarageRating=" + averageRating +
                ", guests=" + guests +
                ", listPhotoFlats=" + listPhotoFlats +
                ", user=" + user +
                '}';
    }
}
