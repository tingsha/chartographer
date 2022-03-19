package com.naname.chartographer.web;

import com.naname.chartographer.data.Canvas;
import com.naname.chartographer.data.CanvasService;
import com.naname.chartographer.data.Fragment;
import com.naname.chartographer.data.FragmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Основной контроллер приложения
 */
@RestController
@Slf4j
@RequestMapping("/chartas")
public class ChartasController {

    private final CanvasService canvasService;
    private final FragmentService fragmentService;

    @Autowired
    public ChartasController(CanvasService canvasService, FragmentService fragmentService) {
        this.canvasService = canvasService;
        this.fragmentService = fragmentService;
    }

    /**
     * Сохранить холст в базу с заданными размерами. Максимальная ширина = 20000, максимальная высота = 50000
     *
     * @param canvas объект с размерами из запроса
     * @return id созданного изображения и статус
     */
    @PostMapping("/")
    public ResponseEntity<Integer> createImage(Canvas canvas) {
        if (canvas.getWidth() > 20000 || canvas.getWidth() <= 0 || canvas.getHeight() > 50000 || canvas.getHeight() <= 0)
            return new ResponseEntity<>(-1, HttpStatus.BAD_REQUEST);

        int id = canvasService.saveCanvas(canvas).getId();
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    /**
     * Сохранить в базу и на диск фрагмент, связанный с холстом {id}
     *
     * @param id       id холста
     * @param fragment объект с размерами и координатами из запроса
     * @return - статус
     */
    @PostMapping("/{id}")
    public ResponseEntity<HttpStatus> restoreImage(@PathVariable(name = "id") int id,
                                                   Fragment fragment,
                                                   @RequestBody MultipartFile image) {
        try {
            Canvas canvas = canvasService.getCanvasById(id);
            if (fragment.getX() >= canvas.getWidth() || fragment.getY() >= canvas.getHeight()
                    || fragment.getX() < 0 || fragment.getY() < 0
                    || fragment.getWidth() > 20000 || fragment.getWidth() <= 0
                    || fragment.getHeight() > 50000 || fragment.getHeight() <= 0)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            fragment.setCanvas(canvas);
            fragment.setId(0);

            Fragment createdFragment = fragmentService.saveFragment(image, fragment);
            fragmentService.removeInnerFragments(canvas, createdFragment);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.info("Canvas with id " + id + " not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получить часть холста с координатами с заданными координатами и размерами.
     * Если запрашиваемая область выходит за границы холста, то лишняя часть закрашивается черным.
     * Максимальная ширина и высота = 5000
     *
     * @param id       холста
     * @param fragment объект с координатами и размерами
     * @return - изображение и статус
     */
    @GetMapping(value = "/{id}", produces = "image/bmp")
    public ResponseEntity<byte[]> getFragment(@PathVariable(name = "id") int id, Fragment fragment) {
        try {
            Canvas canvas = canvasService.getCanvasById(id);
            if (fragment.getWidth() <= 0 || fragment.getWidth() > 5000
                    || fragment.getHeight() <= 0 || fragment.getHeight() > 5000
                    || fragment.getX() < 0 || fragment.getX() >= canvas.getWidth()
                    || fragment.getY() < 0 || fragment.getY() >= canvas.getHeight())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            BufferedImage res = new BufferedImage(fragment.getWidth(), fragment.getHeight(), BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = res.createGraphics();
            g2d.setPaint(Color.BLACK);
            g2d.fillRect(0, 0, fragment.getWidth(), fragment.getHeight());

            fragmentService.paintFragments(canvas, fragment, res);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(res, "bmp", baos);
            return new ResponseEntity<>(baos.toByteArray(), HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.info("Canvas with id " + id + " not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Удалить холст.
     *
     * @param id холста
     * @return статус
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCanvas(@PathVariable(name = "id") int id) {
        try {
            canvasService.deleteCanvas(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            log.info("Canvas with id " + id + " not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}