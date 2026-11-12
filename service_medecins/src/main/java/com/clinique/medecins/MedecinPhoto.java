package com.clinique.medecins;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "medecin_photos")
public class MedecinPhoto {
    @Id Long id;
    @MapsId @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id") Medecin medecin;
    @Column(name = "content_type", nullable = false, length = 20) String contentType;
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "image_data", nullable = false, length = 2097152) byte[] imageData;
    protected MedecinPhoto() {}
}
