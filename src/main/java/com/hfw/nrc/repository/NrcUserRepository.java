package com.hfw.nrc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcUser;

public interface NrcUserRepository extends JpaRepository<NrcUser, Integer> {

	NrcUser findByUserId(String userId);

	/**
	 * Ported verbatim from nrcadmin.repository.NrcUserRepository#findFirstUser
	 * - no ORDER BY, i.e. whatever row the DB happens to return first. Used
	 * only by HospitalAdminService#saveHospitalDetails to stamp createdBy on
	 * the new hr_type_id=25 (bed count) record; this is the source's own
	 * quirky choice (arbitrary "first user in the table", not the calling
	 * admin), not something introduced by this port.
	 */
	@Query(value = "SELECT TOP 1 * FROM nr.nrc_users", nativeQuery = true)
	Optional<NrcUser> findFirstUser();

	/**
	 * Hospital Records list/view/edit (Stage 2): each hospital's own login
	 * account. TOP 1 - a hospital can technically have more than one
	 * nrc_users row (see the earlier-session finding that hospital 1/VVH has
	 * 5), but the Hospital Records screen only needs one representative
	 * username to display/edit, same simplification the source apps already
	 * made everywhere else that assumes one account per hospital.
	 */
	@Query(value = "SELECT TOP 1 * FROM nr.nrc_users WHERE h_id = :hospitalId ORDER BY u_id", nativeQuery = true)
	Optional<NrcUser> findFirstByHospitalId(@Param("hospitalId") Integer hospitalId);
}
