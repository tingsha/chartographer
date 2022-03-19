package com.naname.chartographer;

import com.naname.chartographer.data.Canvas;
import com.naname.chartographer.data.CanvasService;
import com.naname.chartographer.data.Fragment;
import com.naname.chartographer.data.FragmentService;
import com.naname.chartographer.web.ChartasController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChartasControllerUnitTests {

    private final CanvasService canvasService = Mockito.mock(CanvasService.class);

    private final FragmentService fragmentService = Mockito.mock(FragmentService.class);

    private final ChartasController chartasController = Mockito.spy(new ChartasController(canvasService, fragmentService));

    @Test
    void createImage_shouldOk() {
        when(canvasService.saveCanvas(any(Canvas.class))).thenReturn(new Canvas());
        Assertions.assertEquals(HttpStatus.CREATED, chartasController.createImage(new Canvas(1, 1)).getStatusCode());
    }

    @Test
    void restoreImage_shouldOk() throws IOException {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));
        when(fragmentService.saveFragment(any(MultipartFile.class), any(Fragment.class))).thenReturn(new Fragment());

        Assertions.assertEquals(HttpStatus.OK, chartasController.restoreImage(0, new Fragment(0, 0, 1, 1),
                Mockito.mock(MultipartFile.class)).getStatusCode());
    }

    @Test
    void getFragment_shouldOk() {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));

        Assertions.assertEquals(HttpStatus.OK, chartasController.getFragment(0, new Fragment(0, 0, 1, 1)).getStatusCode());
    }

    @Test
    void deleteCanvas_shouldOk() {
        doNothing().when(canvasService).deleteCanvas(any(Integer.class));

        Assertions.assertEquals(HttpStatus.OK, chartasController.deleteCanvas(0).getStatusCode());
    }

    @Test
    void createImage_shouldBadRequest() {
        when(canvasService.saveCanvas(any(Canvas.class))).thenReturn(new Canvas());

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.createImage(new Canvas(0, 100)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.createImage(new Canvas(100, 0)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.createImage(new Canvas(20001, 1)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.createImage(new Canvas(1, 50001)).getStatusCode());
    }

    @Test
    void restoreImage_shouldBadRequest() throws IOException {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));
        when(fragmentService.saveFragment(any(MultipartFile.class), any(Fragment.class))).thenReturn(new Fragment());

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(-1, 0, 100, 100), null).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(0, -1, 100, 100), null).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(0, 0, 0, 100), null).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(0, 0, 100, 0), null).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(0, 0, 20001, 1), null).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.restoreImage(1, new Fragment(0, 0, 1, 50001), null).getStatusCode());
    }

    @Test
    void getFragment_shouldOBadRequest() {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(-1, 0, 1, 1)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(0, -1, 1, 1)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(0, 0, 0, 1)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(0, 0, 1, 0)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(0, 0, 5001, 1)).getStatusCode());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, chartasController.getFragment(0, new Fragment(0, 0, 1, 5001)).getStatusCode());
    }

    @Test
    void restoreImage_shouldNotFound() {
        when(canvasService.getCanvasById(any(Integer.class))).thenThrow(new EntityNotFoundException());

        Assertions.assertEquals(HttpStatus.NOT_FOUND, chartasController.restoreImage(1, new Fragment(1, 1, 1, 1), null).getStatusCode());
    }

    @Test
    void getFragment_shouldNotFound() {
        when(canvasService.getCanvasById(any(Integer.class))).thenThrow(new EntityNotFoundException());

        Assertions.assertEquals(HttpStatus.NOT_FOUND, chartasController.getFragment(1, new Fragment(1, 1, 1, 1)).getStatusCode());
    }

    @Test
    void deleteCanvas_shouldNotFound() {
        doThrow(new EntityNotFoundException()).when(canvasService).deleteCanvas(any(Integer.class));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, chartasController.deleteCanvas(1).getStatusCode());
    }
}