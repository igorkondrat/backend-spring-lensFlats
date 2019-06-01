package backend.spring.controllers;

import backend.spring.dao.FlatDao;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FlatControllerTest {

    private Integer FLAT_ID = 1;

    @Mock
    private FlatDao flatDao;

    @Test(groups = {"exampleDb"})
    public void example() {
        System.out.println("For example - db");
        Assert.assertEquals("2", "1");
    }

    @DataProvider(name = "singleFlatAnonymous")
    public Object[][] getFlatAnonymousDP() {
        return new Object[][]{
                {FLAT_ID}
        };
    }

    @Test(groups = {"unit"}, dataProvider = "singleFlatAnonymous")
    public void getFlatAnonymous(Integer flatId) {
        Assert.assertEquals(flatId, flatId);
    }

    @Test(groups = {"unit"})
    public void getAllFlats() {
    }

    @Test(groups = {"unit"})
    public void singleFlat() {
    }

}
