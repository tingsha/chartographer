package com.naname.chartographer.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * Сервис для работы с холстами.
 */
@Service
public class CanvasService {

    private final CanvasRepository canvasRepository;

    @Autowired
    public CanvasService(CanvasRepository canvasRepository) {
        this.canvasRepository = canvasRepository;
    }

    @Transactional
    public Canvas saveCanvas(Canvas canvas) {
        return canvasRepository.save(canvas);
    }

    public Canvas getCanvasById(int id) throws EntityNotFoundException {
        Optional<Canvas> canvas = canvasRepository.findById(id);
        if (canvas.isEmpty())
            throw new EntityNotFoundException();
        return canvas.get();
    }

    @Transactional
    public void deleteCanvas(int id) {
        Optional<Canvas> canvas = canvasRepository.findById(id);
        if (canvas.isEmpty())
            throw new EntityNotFoundException();
        canvasRepository.deleteById(id);
    }
}
