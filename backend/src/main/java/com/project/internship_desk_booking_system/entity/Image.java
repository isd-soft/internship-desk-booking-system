package com.project.internship_desk_booking_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_seq")
    @SequenceGenerator(
            name = "image_seq",
            sequenceName = "image_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name="file_name")
    private String fileName;

    @Column(name="content_type")
    private String contentType; //MIME type

    @Column(name="image_data")
    private byte[] imageData;

    @Column(name="is_background")
    private boolean isBackground;
}
