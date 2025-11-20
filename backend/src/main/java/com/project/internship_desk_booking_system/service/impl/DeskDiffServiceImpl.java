package com.project.internship_desk_booking_system.service.impl;

import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.Zone;
import com.project.internship_desk_booking_system.service.DeskDiffService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class DeskDiffServiceImpl implements DeskDiffService {

    private <T, R> R safe(T obj, Function<T, R> getter) {
        try {
            return obj == null ? null : getter.apply(obj);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> diff(Desk before, Desk after) {

        Map<String, Object> oldValues = Map.of(
                "Name", before.getDeskName(),
                "Zone", safe(before.getZone(), Zone::getId),
                "Type", before.getType(),
                "Status", before.getStatus(),
                "Temporary availability", before.getIsTemporarilyAvailable(),
                "Temporary window", Arrays.asList(
                        before.getTemporaryAvailableFrom(),
                        before.getTemporaryAvailableUntil()
                )
        );

        Map<String, Object> newValues = Map.of(
                "Name", after.getDeskName(),
                "Zone", safe(after.getZone(), Zone::getId),
                "Type", after.getType(),
                "Status", after.getStatus(),
                "Temporary availability", after.getIsTemporarilyAvailable(),
                "Temporary window", Arrays.asList(
                        after.getTemporaryAvailableFrom(),
                        after.getTemporaryAvailableUntil()
                )
        );

        return oldValues.entrySet().stream()
                .filter(e -> !Objects.equals(e.getValue(), newValues.get(e.getKey())))
                .map(e -> formatMessage(e.getKey(), e.getValue(), newValues.get(e.getKey())))
                .toList();
    }

    private String formatMessage(String field, Object oldVal, Object newVal) {
        if (field.equals("Temporary window")) {
            return "Temporary availability window changed";
        }
        return "%s changed from '%s' to '%s'".formatted(field, oldVal, newVal);
    }

    @Override
    public Desk copy(Desk d) {
        return Desk.builder()
                .deskName(d.getDeskName())
                .zone(d.getZone())
                .type(d.getType())
                .status(d.getStatus())
                .isTemporarilyAvailable(d.getIsTemporarilyAvailable())
                .temporaryAvailableFrom(d.getTemporaryAvailableFrom())
                .temporaryAvailableUntil(d.getTemporaryAvailableUntil())
                .build();
    }
}
