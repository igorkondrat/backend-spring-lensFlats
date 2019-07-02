package backend.spring.controllers;

import backend.spring.dao.FlatDao;
import backend.spring.models.Flat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class FlatControllerTest {

    @InjectMocks
    private FlatController flatController;

    @Mock
    private FlatDao flatDao;

    @BeforeClass
    public void setUp() {
        initMocks(this);
    }

    private Flat flat(String name, int price, double square, int storeys, int rooms, double rating, double averageRating) {
        Flat flat = new Flat();
        flat.setNameFlat(name);
        flat.setPrice(price);
        flat.setSquare(square);
        flat.setStoreys(storeys);
        flat.setRooms(rooms);
        flat.setRating(rating);
        flat.setAverageRating(averageRating);
        return flat;
    }

    private Flat flat = flat("His FLat", 5500, 80.5, 9, 2, 4.5, 4.5);

    private List<Flat> flats = asList(flat("My FLat", 4500, 81.5, 9, 3, 5, 5),
            flat("His FLat", 5500, 1.5, 8, 4, 4, 4.3),
            flat("Our FLat", 400, 0.5, 7, 4, 4.0, 3.8),
            flat("Your FLat", 4567788, 41.5, 6, 43, 3, 3.3));


    @Test(groups = {"exampleDb"})
    public void example() {
        System.out.println("For example - db");
        assertEquals("1", "1");
    }


    @Test(groups = {"unit"})
    public void getAllFlats() {
        when(flatDao.findAll()).thenReturn(flats);
        List<Flat> allFlats = flatController.getAllFlats();
        assertEquals(allFlats, flats);
    }

    @DataProvider(name = "getFlatAnonymousDP")
    public Object[][] getFlatAnonymousDP() {
        return new Object[][]{
                {-24},
                {-0},
                {0},
                {1},
                {20},
                {null}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "getFlatAnonymousDP")
    public void getFlatAnonymous(Integer flatId) {
        when(flatDao.getFlatById(flatId)).thenReturn(flat);
        Flat flatAnonymous = flatController.getFlatAnonymous(flatId);
        assertEquals(flatAnonymous, flat);
    }

    @DataProvider(name = "starFilterDP")
    public Object[][] starFilterDP() {
        List<Flat> allFlats = asList(flat("My FLat", 4500, 81.5, 9, 3, 5, 5),
                flat("My FLat", 4500, 81.5, 9, 3, 5, 5),
                flat("His FLat", 5500, 1.5, 8, 4, 4, 4.3),
                flat("His FLat", 5500, 1.5, 8, 4, 4, 4.3));
        List<Flat> expectedFlats = asList(flat("My FLat", 4500, 81.5, 9, 3, 5, 5),
                flat("My FLat", 4500, 81.5, 9, 3, 5, 5));
        List<Flat> expectedFlats2 = asList(flat("His FLat", 5500, 1.5, 8, 4, 4, 4.3),
                flat("His FLat", 5500, 1.5, 8, 4, 4, 4.3));
        return new Object[][]{
                {5, 4000, 5000, 3, allFlats, expectedFlats},
                {4, 100, 100000, 4, allFlats, expectedFlats2},
        };
    }

    @Test(groups = {"unit"}, dataProvider = "starFilterDP")
    public void starFilter(int rating, int minPrice, int maxPrice, int rooms, List<Flat> allFlats, List<Flat> expectedFlats) {
        when(flatDao.findAll()).thenReturn(allFlats);
        List<Flat> filteredFlatList = flatController.starFilter(rating, minPrice, maxPrice, rooms);
        assertEquals(filteredFlatList, expectedFlats);
    }

}
