package io.academic.entity;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface OaiRecordRepository extends PagingAndSortingRepository<OaiRecord, Long> {
    List<OaiRecord> findById(UUID Id);
}
