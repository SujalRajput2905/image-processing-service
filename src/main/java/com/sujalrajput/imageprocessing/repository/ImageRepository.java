package com.sujalrajput.imageprocessing.repository;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findAllByUser(User user, Pageable pageable);

    Optional<Image> findByFileNameAndUser(String fileName, User user);
}
