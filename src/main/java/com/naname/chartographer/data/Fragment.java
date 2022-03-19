package com.naname.chartographer.data;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Сущность фрагмента. Каждый фрагмент связан с холстом и имеет дату добавления, чтобы при
 * отрисовки на холсте накладывать новые фрагменты на старые
 */
@Entity
@Getter
@Setter
public class Fragment {

    public Fragment() {
    }

    public Fragment(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Canvas canvas;
    private int width;
    private int height;
    private int x;
    private int y;
    private ZonedDateTime date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Fragment fragment = (Fragment) o;
        return Objects.equals(id, fragment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Fragment{" +
                "id=" + id +
                ", canvas=" + canvas +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
