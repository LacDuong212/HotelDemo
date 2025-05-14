package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Images;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {
    @Query("SELECT i FROM Images i WHERE i.oid = :hid ORDER BY i.stt ASC")
    List<Images> findImagesByHid(Long hid);
	@Query("SELECT i FROM Images i WHERE i.oid = :rid ORDER BY i.stt ASC")
    List<Images> findImagesByRid(Long rid);
	List<Images> findByOidOrderBySttAsc(Long oid);
    Images findByOidAndStt(Long oid, int stt);
    @Query("SELECT MAX(i.stt) FROM Images i WHERE i.oid = :oid")
    Integer findMaxSttByOid(@Param("oid") Long oid);
    List<Integer> findSttByOid(Long oid);

    @Query("SELECT i.iid FROM Images i WHERE i.oid = :oid ORDER BY i.stt ASC")
    List<Long> findIidByOid(@Param("oid") Long oid);

    @Transactional
    void deleteAllByOid(Long rid);
}
