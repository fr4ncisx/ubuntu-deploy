package com.ubuntu.ubuntu_app.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ubuntu.ubuntu_app.Repository.ImageRepository;
import com.ubuntu.ubuntu_app.Repository.PublicationRepository;
import com.ubuntu.ubuntu_app.Repository.PublicationViewRepository;
import com.ubuntu.ubuntu_app.configuration.MapperConverter;
import com.ubuntu.ubuntu_app.infra.date.GlobalDate;
import com.ubuntu.ubuntu_app.infra.errors.IllegalParameterException;
import com.ubuntu.ubuntu_app.infra.errors.IllegalRewriteException;
import com.ubuntu.ubuntu_app.infra.errors.SqlEmptyResponse;
import com.ubuntu.ubuntu_app.infra.statuses.ResponseMap;
import com.ubuntu.ubuntu_app.model.dto.PublicationDTO;
import com.ubuntu.ubuntu_app.model.dto.PublicationRequestDTO;
import com.ubuntu.ubuntu_app.model.dto.PublicationStatisticsDTO;
import com.ubuntu.ubuntu_app.model.entities.ImageEntity;
import com.ubuntu.ubuntu_app.model.entities.PublicationEntity;
import com.ubuntu.ubuntu_app.model.entities.PublicationViewEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PublicationService {

    private final PublicationRepository publicationRepository;
    private final PublicationViewRepository publicationViewRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public ResponseEntity<?> create(PublicationRequestDTO publicationsDTO) {
        var imageEntity = publicationsDTO.getImagenes().stream()
                .map(imgDTO -> MapperConverter.generate().map(imgDTO, ImageEntity.class)).toList();
        PublicationEntity publicationEntity = new PublicationEntity(publicationsDTO, imageEntity);
        publicationRepository.save(publicationEntity);
        var jsonResponse = ResponseMap.createResponse("Created publication");
        return new ResponseEntity<>(jsonResponse, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> update(PublicationRequestDTO publicationsDTO, Long id) {
        var publicationFound = publicationFinder(id);
        var imageDTO = publicationsDTO.getImagenes();
        var imageEntity = publicationFound.getImagenes();
        if (imageDTO.size() == imageEntity.size()) {
            for (int i = 0; i < imageDTO.size(); i++) {
                imageEntity.get(i).setUrl(imageDTO.get(i).getUrl());
            }
            publicationFound.edit(publicationsDTO, imageEntity);
        } else {
            var convertedImgEntity = publicationsDTO.getImagenes().stream()
                    .map(img -> MapperConverter.generate().map(img, ImageEntity.class)).toList();
            publicationFound.edit(publicationsDTO, convertedImgEntity);
            imageRepository.cleanOrphanImages();
        }
        return ResponseEntity.ok(ResponseMap.createResponse("Updated succesfully"));
    }

    public ResponseEntity<?> findById(Long id) {
        var publicationFound = publicationFinder(id);
        var jsonResponse = MapperConverter.generate().map(publicationFound, PublicationDTO.class);
        return ResponseEntity.ok(jsonResponse);
    }

    @Transactional
    public ResponseEntity<?> disablePublication(Long id) {
        var publicationFound = publicationFinder(id);
        if (!publicationFound.isActive()) {
            throw new IllegalRewriteException("Publication is already hidden");
        }
        publicationFound.setActive(false);
        return ResponseEntity.ok(ResponseMap.createResponse("Hidden publication sucessfully"));
    }

    @Transactional
    public ResponseEntity<?> viewed(Long id) {
        var publicationFound = publicationFinder(id);
        PublicationViewEntity view = new PublicationViewEntity(publicationFound);
        publicationViewRepository.save(view);
        return ResponseEntity.ok(ResponseMap.createResponse("Added new visualization"));
    }

    public ResponseEntity<?> findAllActiveRecentPublications() {
        var listOfPublications = publicationRepository.findAllByActiveTrueOrderByDateDesc();
        if (listOfPublications.isEmpty()) {
            throw new SqlEmptyResponse("No publications found");
        }
        var responseDTO = listOfPublications.stream()
                .map(entity -> MapperConverter.generate().map(entity, PublicationDTO.class)).toList();
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> findAll() {
        var listOfPublications = publicationRepository.findAll();
        if (listOfPublications.isEmpty()) {
            throw new SqlEmptyResponse("No publications found");
        }
        var responseDTO = listOfPublications.stream()
                .map(entity -> MapperConverter.generate().map(entity, PublicationDTO.class)).toList();
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getStatistics(Long limitSize) {
        List<PublicationStatisticsDTO> statistics = new ArrayList<>();
        Long publicationsCount = publicationRepository.count();
        Long Id = null;
        if (limitSize <= 0) {
            throw new IllegalParameterException("Expected limitSize above 0");
        }
        if (publicationsCount == 0) {
            throw new SqlEmptyResponse("Empty publications");
        }
        for (int i = 1; i <= publicationsCount; i++) {
            Id = Long.valueOf(i);
            var publicationEntityOptional = publicationRepository.findByIdCurrentMonthAndActive(Id,
                    GlobalDate.getCurrentMonth(), GlobalDate.getCurrentYear());
            if (!publicationEntityOptional.isPresent()) {
                continue;
            }
            var publicationEntity = publicationEntityOptional.get();
            var count = publicationViewRepository.getClickCountActualMonth(Id, GlobalDate.getCurrentMonth(),
                    GlobalDate.getCurrentYear());
            statistics.add(
                    new PublicationStatisticsDTO(publicationEntity.getTitle(), publicationEntity.getDate(), count));
        }
        var sortedStatistics = statistics.stream()
                .sorted(Comparator.comparing(PublicationStatisticsDTO::getVisualizations).reversed())
                .limit(limitSize)
                .toList();
        if (sortedStatistics.isEmpty()) {
            throw new SqlEmptyResponse("No publications at current month/year");
        }
        return new ResponseEntity<>(sortedStatistics, HttpStatus.OK);
    }

    private PublicationEntity publicationFinder(Long id) {
        var optionalPublication = publicationRepository.findById(id);
        if (!optionalPublication.isPresent()) {
            throw new SqlEmptyResponse("Publication not found");
        } else {
            return optionalPublication.get();
        }
    }

    public ResponseEntity<?> findPublication(String publication) {
        if(publication.isBlank() || publication == null){
            throw new IllegalParameterException("Input is required");
        }
        publication = "%" + publication + "%";
        var listOfPublications = publicationRepository.findByTitleLikeAndActiveTrue(publication);
        if(listOfPublications.isEmpty()){
            throw new SqlEmptyResponse("No match found");
        }
        var responseDTO = listOfPublications.stream().map(list -> MapperConverter.generate().map(list, PublicationDTO.class));
        return ResponseEntity.ok(responseDTO);
    }
}
