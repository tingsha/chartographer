package com.naname.chartographer.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Сущность холста. Холст содержит список фрагментов, связанных с ним.
 */
@Entity
@Getter
@Setter
@ToString
public class Canvas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int height;
    private int width;

    @OneToMany(mappedBy = "canvas", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Fragment> fragments;

    public Canvas() {
    }

    public Canvas(int width, int height) {
        this.height = height;
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Canvas canvas = (Canvas) o;
        return Objects.equals(id, canvas.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
