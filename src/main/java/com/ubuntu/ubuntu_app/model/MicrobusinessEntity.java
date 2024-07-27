package com.ubuntu.ubuntu_app.model;

import java.util.List;
import java.util.stream.Collectors;

import com.ubuntu.ubuntu_app.configuration.MapperConverter;
import com.ubuntu.ubuntu_app.model.dto.ImageDTO;
import com.ubuntu.ubuntu_app.model.dto.MicrobusinessDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Microemprendimientos")
public class MicrobusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    @Column(name = "mas_informacion")
    private String masInformacion;
    private String pais;
    private String provincia;
    private String ciudad;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "id_categoria")
    private CategoryEntity categoria;
    private String subcategoria;
    private boolean activo;
    private boolean gestionado;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<ImageEntity> imagenes;

    public MicrobusinessEntity(MicrobusinessDTO microbusinessDTO, CategoryEntity categoria) {
        this.nombre = microbusinessDTO.getNombre();
        this.descripcion = microbusinessDTO.getDescripcion();
        this.masInformacion = microbusinessDTO.getMasInformacion();
        this.pais = microbusinessDTO.getPais();
        this.provincia = microbusinessDTO.getProvincia();
        this.ciudad = microbusinessDTO.getCiudad();
        this.categoria = categoria;
        this.subcategoria = microbusinessDTO.getSubcategoria();
        this.activo = true;
        this.gestionado = false;
        this.imagenes = convertDTOtoEntity(microbusinessDTO.getImagenes());
    }

    public void edit(MicrobusinessDTO microbusinessDTO, CategoryEntity categoria) {
        this.nombre = microbusinessDTO.getNombre();
        this.descripcion = microbusinessDTO.getDescripcion();
        this.masInformacion = microbusinessDTO.getMasInformacion();
        this.pais = microbusinessDTO.getPais();
        this.provincia = microbusinessDTO.getProvincia();
        this.ciudad = microbusinessDTO.getCiudad();
        this.categoria = categoria;
        this.subcategoria = microbusinessDTO.getSubcategoria();
        // this.imagenes = microbusinessDTO.getImagenes();
    }

    public List<ImageEntity> convertDTOtoEntity(List<ImageDTO> dto) {
        return dto.stream()
                .map(i -> MapperConverter.generate().map(i, ImageEntity.class))
                .collect(Collectors.toList());
    }
}
