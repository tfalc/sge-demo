package br.com.sge.modules.galeria.repository;

import br.com.sge.modules.galeria.entity.GaleriaFoto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GaleriaFotoRepository extends JpaRepository<GaleriaFoto, UUID> {

    @Query("select f from GaleriaFoto f where f.album.id = :albumId order by f.ordem asc, f.enviadoEm asc")
    List<GaleriaFoto> findByAlbumId(@Param("albumId") UUID albumId);

    @Query("select f from GaleriaFoto f join fetch f.album where f.id = :id")
    Optional<GaleriaFoto> findDetalhada(@Param("id") UUID id);

    int countByAlbumId(UUID albumId);
}
