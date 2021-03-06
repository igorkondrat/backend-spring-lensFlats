package backend.spring.controllers;

import backend.spring.dao.FlatDao;
import backend.spring.dao.UserDao;
import backend.spring.models.Flat;
import backend.spring.models.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.getProperty;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.util.Comparator.comparingInt;
import static java.util.UUID.randomUUID;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class FlatController {

    private UserDao userDao;
    private FlatDao flatDao;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public FlatController(FlatDao flatDao, UserDao userDao) {
        this.flatDao = flatDao;
        this.userDao = userDao;
    }

    @GetMapping("/getAllFlats")
    public List<Flat> getAllFlats() {
        return flatDao.findAll();
    }

    //TODO Rewrite. Method could not return null
    @GetMapping("/getSingleFlat/{flatId}")
    public Flat getFlat(@PathVariable("flatId") Integer flatId) {
        for (Flat flat : userDao.findByEmail(getContext().getAuthentication().getName()).getFlatList()) {
            if (flat.getId() == flatId) {
                return flat;
            }
        }
        return null;
    }

    @GetMapping("/getSingleFlatAnonymous/{flatId}")
    public Flat getFlatAnonymous(@PathVariable("flatId") Integer flatId) {
        return flatDao.getFlatById(flatId);
    }

    //TODO Rename standartFlatPicture (front and back) + optimize code
    @PostMapping("/flatRegister")
    public String flatRegister(@RequestBody Flat flat) {
        User user = userDao.findByEmail(getContext().getAuthentication().getName());
        if (user != null && flat.getRooms() > 0 && flat.getSquare() > 0) {
            flat.setUser(user);
            flat.setPhotoFlats("standartFlatPicture.png");
            flat.setAverageRating(0.0);
            flat.setWallingMaterial("");
            flatDao.save(flat);
            return JSONObject.quote("You created flat");
        } else return JSONObject.quote("Something wrong :(");
    }

    @PostMapping("/updateFlat")
    public String updateFlat(@RequestBody Flat flat) {
        flat.setUser(userDao.findByEmail(getContext().getAuthentication().getName()));
        flatDao.getFlatById(flat.getId()).getListPhotoFlats().forEach(flat::setPhotoFlats);
        Flat flatById = flatDao.getFlatById(flat.getId());
        if (flatById != null
                && flat.getSquare() > 0
                && flat.getRooms() > 0
                && flat.getPrice() > 0
                && flat.getFloor() > 0
                && flat.getGuests() > 0
                && flat.getStoreys() > 0) {
            flat.setAverageRating(flatById.getAverageRating());
            flatById.setWallingMaterial(flat.getWallingMaterial());
            flatById.getRating().forEach(flat::setRating);
            flatDao.save(flat);
            return JSONObject.quote("Flat was updated");
        }
        return JSONObject.quote("Something wrong");
    }

    @PostMapping("/deleteFlat{flatId}")
    public void deleteFlat(@PathVariable Integer flatId) throws IOException {
        User user = userDao.findByEmail(getContext().getAuthentication().getName());
        List<Flat> flatList = user.getFlatList();
        String baseDir = getProperty("user.dir");
        for (Flat flat : flatList) {
            if (flat.getId() == flatId) {
                ArrayList<String> listPhotoFlats = flat.getListPhotoFlats();
                for (String flatPhoto : listPhotoFlats) {
                    deleteAllFlatPhoto(baseDir, flatPhoto);
                }
            }
        }
        user.getFlatList().removeIf(flat -> flat.getId() == flatId);
        flatDao.delete(flatDao.getFlatById(flatId));
        userDao.save(user);
    }

    @PostMapping("/deleteFlatPhoto/{flatPhoto}&{flatId}")
    public String deleteFlatPhoto(@PathVariable("flatPhoto") String flatPhoto,
                                  @PathVariable("flatId") Integer flatId) throws IOException {
        User user = userDao.findByEmail(getContext().getAuthentication().getName());
        String baseDir = getProperty("user.dir");
        if (user != null) {
            List<Flat> flatList = user.getFlatList();
            for (Flat flat : flatList) {
                if (flat.getId() == flatId) {
                    flat.getListPhotoFlats().removeIf(photo -> photo.equals(flatPhoto));
                    if (flat.getListPhotoFlats().size() < 1) {
                        flat.setPhotoFlats("standartFlatPicture.png");
                    }
                }
            }
            deleteAllFlatPhoto(baseDir, flatPhoto);
            userDao.save(user);
        }
        return JSONObject.quote("Photo was removed");
    }

    private void deleteAllFlatPhoto(String baseDir, String listPhotoFlat) throws IOException {
        if (exists(Paths.get(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + listPhotoFlat))
                && !listPhotoFlat.equals("standartFlatPicture.png")) {
            delete(Paths.get(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + listPhotoFlat));
        }
    }

    //TODO Do something with mkdir() and createNewFile()
    @PostMapping("/changePhotoFlat{id}")
    public String changePhotoFlat(@RequestPart("newPhotoFlat") MultipartFile[] photo,
                                  @PathVariable Integer id) throws IOException {
        User user = userDao.findByEmail(getContext().getAuthentication().getName());
        String baseDir = getProperty("user.dir");
        File uploadFolder = new File(baseDir + uploadPath + "flatsPicture");
        if (!uploadFolder.exists()) {
            uploadFolder.mkdir();
        }
        for (MultipartFile multipartFile : photo) {
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            if (image != null) {
                for (int j = 0; j < user.getFlatList().size(); j++) {
                    if (user.getFlatList().get(j).getId() == id) {
                        Flat flat = user.getFlatList().get(j);
                        if (flat.getListPhotoFlats().size() < 10) {
                            if (flat.getListPhotoFlats().contains("standartFlatPicture.png")) {
                                System.out.println("FlatController.class -> changePhotoFlat() | standart flat photo removed");
                                flat.getListPhotoFlats().removeIf(next -> next.equals("standartFlatPicture.png"));
                            }
                            String uuidFile = randomUUID().toString();
                            String resultFileName = uuidFile + multipartFile.getOriginalFilename();
                            final File targetFile = new File(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + resultFileName);
                            targetFile.createNewFile();
                            multipartFile.transferTo(targetFile);
                            flat.setPhotoFlats(resultFileName);
                            flatDao.save(flat);
                            System.out.println("FlatController.class -> changePhotoFlat() | flat photo update");
                        } else return JSONObject.quote("Maximum 10 photo");
                    }
                }
            } else return JSONObject.quote("This is not an image file");
        }
        return JSONObject.quote("Photo was updated");
    }

    @GetMapping("/rateFlat/{flatId}/{rating}")
    public String rate(@PathVariable("flatId") Integer flatId,
                       @PathVariable("rating") double newRating) {
        Flat flat = flatDao.getFlatById(flatId);
        if (flat != null) {
            flat.setRating(newRating);
            ArrayList<Double> rating = flat.getRating();
            double sum = 0;
            for (Double rate : rating) {
                sum += rate;
            }
            flat.setAverageRating(sum / rating.size());
            flatDao.save(flat);
        }
        return JSONObject.quote("Rated");
    }

    @GetMapping("/getFilter/{rating}&{minPrice}&{maxPrice}&{rooms}")
    public List<Flat> getFilter(@PathVariable("rating") int rating,
                                @PathVariable("minPrice") int minPrice,
                                @PathVariable("maxPrice") int maxPrice,
                                @PathVariable("rooms") int rooms) {
        List<Flat> filteredFlatList = getAllFlats()
                .stream()
                .filter(flat -> flat.getAverageRating() >= rating && flat.getAverageRating() < rating + 1)
                .filter(flat -> flat.getPrice() >= minPrice && flat.getPrice() <= maxPrice)
                .sorted(comparingInt(Flat::getPrice))
                .collect(Collectors.toList());
        if (rooms != 0) {
            if (rooms == 5) {
                filteredFlatList.removeIf(next -> next.getRooms() < rooms);
            } else {
                filteredFlatList.removeIf(next -> next.getRooms() != rooms);
            }
        }
        if(rooms == 0 && rating == 0){
            List<Flat> flatList = getAllFlats()
                    .stream()
                    .filter(flat -> flat.getPrice() >= minPrice && flat.getPrice() <= maxPrice)
                    .sorted(comparingInt(Flat::getPrice))
                    .collect(Collectors.toList());
            filteredFlatList.clear();
            filteredFlatList.addAll(flatList);
        }
        return filteredFlatList;
    }

}
