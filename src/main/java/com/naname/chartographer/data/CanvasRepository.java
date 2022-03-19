package com.naname.chartographer.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanvasRepository extends CrudRepository<Canvas, Integer> {
}
