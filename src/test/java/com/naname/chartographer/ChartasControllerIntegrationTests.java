package com.naname.chartographer;

import com.naname.chartographer.data.Canvas;
import com.naname.chartographer.data.CanvasService;
import com.naname.chartographer.data.Fragment;
import com.naname.chartographer.data.FragmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ChartasControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CanvasService canvasService;

    @MockBean
    private FragmentService fragmentService;

    @MockBean
    private ChartographerApplication chartographerApplication;

    private ApplicationArguments arguments = Mockito.mock(ApplicationArguments.class);
    private HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    @Test
    void createImage_shouldOk() throws Exception {
        when(chartographerApplication.getDataSource()).thenReturn(DataSourceBuilder
                .create()
                .username("root")
                .password("toor")
                .url("jdbc:h2:file:./data/chartographer")
                .driverClassName("org.h2.Driver")
                .build());
        when(canvasService.saveCanvas(any(Canvas.class))).thenReturn(new Canvas(100, 100));

        mockMvc.perform(post("/chartas/?width=100&height=100"))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("0")));
        verify(canvasService).saveCanvas(new Canvas(100, 100));
    }

    @Test
    void restoreImage_shouldOk() throws Exception {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));
        when(fragmentService.saveFragment(any(MultipartFile.class), any(Fragment.class))).thenReturn(new Fragment(0, 0, 100, 100));

        mockMvc.perform(post("/chartas/1/?x=0&y=0&width=100&height=100"))
                .andExpect(status().isOk());
        verify(canvasService).getCanvasById(1);
        verify(fragmentService).saveFragment(null, new Fragment(0, 0, 100, 100));
    }

    @Test
    void getFragment_shouldOk() throws Exception {
        when(canvasService.getCanvasById(any(Integer.class))).thenReturn(new Canvas(100, 100));

        mockMvc.perform(get("/chartas/1/?x=0&y=0&width=100&height=100"))
                .andExpect(status().isOk());
        verify(canvasService).getCanvasById(1);
    }

    @Test
    void deleteCanvas_shouldOk() throws Exception {
        doNothing().when(canvasService).deleteCanvas(1);

        mockMvc.perform(delete("/chartas/1/"))
                .andExpect(status().isOk());
        verify(canvasService).deleteCanvas(1);
    }
}
