package com.project.internship_desk_booking_system.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.InputStream;
import java.sql.PreparedStatement;

public class V19__Insert_images extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/images/Floor.png")) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: /images/Floor.png");
            }

            byte[] data = is.readAllBytes();

            String sql = """
                INSERT INTO images (file_name, content_type, image_data, is_background)
                VALUES (?, ?, ?, true)
            """;

            try (PreparedStatement st = context.getConnection().prepareStatement(sql)) {
                st.setString(1, "Floor.png");
                st.setString(2, "image/png");
                st.setBytes(3, data);
                st.executeUpdate();
            }
        }
    }
}
