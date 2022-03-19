package com.naname.chartographer;

import com.naname.chartographer.data.Canvas;
import com.naname.chartographer.data.Fragment;
import com.naname.chartographer.data.FragmentRepository;
import com.naname.chartographer.data.FragmentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FragmentServiceUnitTests {

    private final FragmentService fragmentService = new FragmentService(Mockito.mock(FragmentRepository.class));

    @Test
    void isSecondFragmentInner_shouldFalse() {
        assertFalse(fragmentService.isSecondFragmentInner(new Fragment(0, 0, 100, 100),
                new Fragment(0, 0, 101, 101)));
        assertFalse(fragmentService.isSecondFragmentInner(new Fragment(0, 0, 50, 50),
                new Fragment(51, 51, 100, 100)));
        assertFalse(fragmentService.isSecondFragmentInner(new Fragment(10, 10, 50, 50),
                new Fragment(0, 0, 100, 100)));
    }

    @Test
    void isSecondFragmentInner_shouldTrue() {
        assertTrue(fragmentService.isSecondFragmentInner(new Fragment(0, 0, 100, 100),
                new Fragment(0, 0, 100, 100)));
        assertTrue(fragmentService.isSecondFragmentInner(new Fragment(0, 0, 50, 50),
                new Fragment(0, 0, 20, 20)));
        assertTrue(fragmentService.isSecondFragmentInner(new Fragment(0, 0, 50, 50),
                new Fragment(10, 10, 20, 20)));
    }

    @Test
    void getIncidentFragments_shouldNotEmpty() {
        when(fragmentService.getFragments(any(Canvas.class))).thenReturn(
                List.of(new Fragment(0, 0, 100, 100),
                        new Fragment(101, 100, 100, 100))
        );

        assertEquals(1, fragmentService.getIncidentFragments(new Canvas(),
                new Fragment(20, 20, 30, 30)).size());
        assertEquals(2, fragmentService.getIncidentFragments(new Canvas(),
                new Fragment(100, 100, 10, 10)).size());
    }

    @Test
    void getIncidentFragments_shouldEmpty() {
        when(fragmentService.getFragments(any(Canvas.class))).thenReturn(
                List.of(new Fragment(0, 0, 100, 100),
                        new Fragment(103, 100, 100, 100))
        );

        assertEquals(0, fragmentService.getIncidentFragments(new Canvas(),
                new Fragment(300, 300, 30, 30)).size());
        assertEquals(0, fragmentService.getIncidentFragments(new Canvas(),
                new Fragment(101, 0, 1, 400)).size());
    }

    @Test
    void getInsertPosition_shouldReturnNumber() {
        assertEquals(0, fragmentService.getInsertPosition(70, 70, 70, 100));
        assertEquals(20, fragmentService.getInsertPosition(90, 70, 70, 100));
    }
}
