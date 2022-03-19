package com.naname.chartographer.data;

import com.naname.chartographer.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.file.SimplePathVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для обработки фрагментов
 */
@Service
@Slf4j
public class FragmentService {

    private final FragmentRepository fragmentRepository;

    @Autowired
    public FragmentService(FragmentRepository fragmentRepository) {
        this.fragmentRepository = fragmentRepository;
    }

    /**
     * Сохранить фрагмент. Дата нужна для определения более новых фрагментов
     *
     * @param image связанный холст
     * @return созданный фрагмент
     * @throws IOException если не удалось сохранить на диск
     */
    @Transactional
    public Fragment saveFragment(MultipartFile image, Fragment fragment) throws IOException {
        fragment.setDate(ZonedDateTime.now(ZoneId.of("Europe/Moscow")));
        fragmentRepository.save(fragment);
        saveImage(image, fragment.getId());
        return fragment;
    }

    public List<Fragment> getFragments(Canvas canvas) {
        return fragmentRepository.getFragmentsByCanvasOrderByDate(canvas);
    }

    /**
     * Удалить из базы и диска изображения с заданным id (имя изображения совпадает с id)
     *
     * @param id связанный id фрагмента в базе
     */
    public void removeFragment(int id) throws IOException {
        Files.walkFileTree(FileUtil.getFragmentsAbsolutePath().toPath(), new SimplePathVisitor() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().equals(id + ".bmp"))
                    Files.delete(file);
                return FileVisitResult.TERMINATE;
            }
        });
        fragmentRepository.deleteById(id);
    }

    /**
     * Сохраненить фрагмент на диск, название файла совпадает с id
     *
     * @param id связанный id фрагмента в базе
     */
    public void saveImage(MultipartFile multipartFile, int id) throws IOException {
        Path imageFile = Path.of(FileUtil.getFragmentsAbsolutePath() + "/" + id + ".bmp");
        try {
            Files.createFile(imageFile);
        } catch (FileAlreadyExistsException e) {
            log.info("Image " + imageFile + " already exists");
        }

        multipartFile.transferTo(imageFile);
    }

    /**
     * Удалить фрагменты, которые перекрывает sourceFragment
     */
    @Transactional
    public void removeInnerFragments(Canvas canvas, Fragment sourceFragment) throws IOException {
        List<Fragment> incidentFragments = getIncidentFragments(canvas, sourceFragment);
        incidentFragments.remove(sourceFragment);
        for (Fragment fragment : incidentFragments) {
            if (isSecondFragmentInner(sourceFragment, fragment)) {
                log.info("Removed " + fragment);
                removeFragment(fragment.getId());
            }
        }
    }

    /**
     * Отрисовать фрагменты на запрашиваемой области.
     * Хитрыми математическими вычислениями получает расположение фрагментов на области.
     *
     * @param canvas         холст
     * @param sourceFragment запрашиваемая область
     * @param res            изображение, в которое запишется область
     */
    public void paintFragments(Canvas canvas, Fragment sourceFragment, BufferedImage res) throws IOException {
        int y = sourceFragment.getY();
        int x = sourceFragment.getX();
        int width = sourceFragment.getWidth();
        int height = sourceFragment.getHeight();
        List<Fragment> incidentFragments = getIncidentFragments(canvas, sourceFragment);

        for (Fragment fragment : incidentFragments) {
            BufferedImage fragmentImage = ImageIO.read(loadImageFromFile(fragment.getId()));

            int startY = Math.max(fragment.getY(), y);
            int startX = Math.max(fragment.getX(), x);

            int endY = Math.min(y + height, Math.min(fragment.getY() + fragment.getHeight(), canvas.getHeight()));
            int endX = Math.min(x + width, Math.min(fragment.getX() + fragment.getWidth(), canvas.getWidth()));

            for (int i = startY; i < endY; i++) {
                //int y0 = Math.min(height, canvas.getHeight() - y) - (Math.min(y + height, canvas.getHeight()) - i);
                int y0 = getInsertPosition(i, y, fragment.getHeight(), canvas.getHeight());
                int y1 = i - fragment.getY();
                for (int j = startX; j < endX; j++) {
                    //int x0 = Math.min(width, canvas.getWidth() - x) - (Math.min(x + width, canvas.getWidth()) - j);
                    int x0 = getInsertPosition(j, x, fragment.getWidth(), canvas.getWidth());
                    int x1 = j - fragment.getX();
                    res.setRGB(x0, y0, fragmentImage.getRGB(x1, y1));
                }
            }
        }
    }

    /**
     * Загрузить изображения с диска
     *
     * @param id фрагмента
     * @return файл-изображение
     */
    public File loadImageFromFile(int id) throws IOException {
        final Path[] imageFile = new Path[1]; // чтобы изменить переменную во внутреннем классе, оборачиваем ее в массив
        Files.walkFileTree(FileUtil.getFragmentsAbsolutePath().toPath(), new SimplePathVisitor() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().equals(id + ".bmp")) {
                    imageFile[0] = Path.of(FileUtil.getFragmentsAbsolutePath() + "/" + id + ".bmp");
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (imageFile[0] == null)
            throw new FileNotFoundException("File " + FileUtil.getFragmentsAbsolutePath() + id + ".bmp not found");
        return imageFile[0].toFile();
    }

    /**
     * Определить, вложен ли second в first
     *
     * @param first  первый фрагмент
     * @param second потенциально вложенный фрагмент
     */
    public boolean isSecondFragmentInner(Fragment first, Fragment second) {
        return second.getX() >= first.getX()
                && second.getX() + second.getWidth() <= first.getX() + first.getWidth()
                && second.getY() >= first.getY()
                && second.getY() + second.getHeight() <= first.getY() + first.getHeight();
    }

    /**
     * Метод убирает дублирование в #paintFragments (изначальный вариант закоментирован).
     * Предназначен для нахождения координат в запрашиваемой области, куда надо вставлять пиксель фрагмента.
     *
     * @param current           текущий пиксель фрагмента
     * @param coord             x или y
     * @param fragmentDimension длина или высота фрагмента (зависит от coord)
     * @param canvasDimension   длина или ширина холста (зависит от coord)
     * @return координату x или y (зависит от coord) области, в которую надо вставлять пиксель фрагмента
     */
    public int getInsertPosition(int current, int coord, int fragmentDimension, int canvasDimension) {
        return Math.min(fragmentDimension, canvasDimension - coord)
                - (Math.min(coord + fragmentDimension, canvasDimension) - current);
    }

    /**
     * Список фрагментов, которые пересекаются с sourceFragment
     */
    public List<Fragment> getIncidentFragments(Canvas canvas, Fragment sourceFragment) {
        return getFragments(canvas).stream()
                .filter(f -> f.getX() + f.getWidth() >= sourceFragment.getX()
                        && sourceFragment.getX() + sourceFragment.getWidth() >= f.getX()
                        && f.getY() + f.getHeight() >= sourceFragment.getY()
                        && sourceFragment.getY() + sourceFragment.getHeight() >= f.getY())
                .collect(Collectors.toList());
    }
}
