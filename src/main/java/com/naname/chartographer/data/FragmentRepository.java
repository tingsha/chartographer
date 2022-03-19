package com.naname.chartographer.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FragmentRepository extends JpaRepository<Fragment, Integer> {

    List<Fragment> getFragmentsByCanvasOrderByDate(Canvas canvas);
}
