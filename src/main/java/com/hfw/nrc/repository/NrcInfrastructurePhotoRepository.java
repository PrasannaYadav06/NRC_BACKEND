package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcInfrastructurePhoto;

public interface NrcInfrastructurePhotoRepository extends JpaRepository<NrcInfrastructurePhoto, Integer> {

	/** Same "HId" derived-query naming gotcha as NrcInfrastructureRepository#findByHId - explicit @Query, see that one's javadoc. */
	@Query("select p from NrcInfrastructurePhoto p where p.hId = :hId order by p.uploadedOn desc")
	List<NrcInfrastructurePhoto> findByHIdOrderByUploadedOnDesc(@Param("hId") int hId);
}
