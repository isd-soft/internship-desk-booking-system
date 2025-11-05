package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.FavouriteDesks;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.enums.DeskStatus;
import com.project.internship_desk_booking_system.enums.DeskType;
import com.project.internship_desk_booking_system.repository.DeskRepository;
import com.project.internship_desk_booking_system.repository.FavouriteDesksRepository;
import com.project.internship_desk_booking_system.repository.UserRepository;
import com.project.internship_desk_booking_system.service.FavouriteDesksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FavouriteDesksServiceTest {

    @Mock
    private FavouriteDesksRepository favouriteDesksRepository;

    @Mock
    private DeskRepository deskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavouriteDesksService favouriteDesksService;

    private User user1;
    private User user2;
    private Desk desk2;
    private Desk desk3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = mock(User.class);
        when(user1.getId()).thenReturn(1L);
        when(user1.getEmail()).thenReturn("maria.florea@gmail.com");

        user2 = mock(User.class);
        when(user2.getId()).thenReturn(2L);
        when(user2.getEmail()).thenReturn("ion.paun@gmail.com");

        desk2 = new Desk(
                "Serv 101",
                "Serv",
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                true,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(5)
        );
        desk2.setId(2L);

        desk3 = new Desk(
                "PLC 201",
                "PLC",
                DeskType.SHARED,
                DeskStatus.ACTIVE,
                true,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(5)
        );
        desk3.setId(3L);
    }

    // -----------------------------
    // addFavouriteDesk tests
    // -----------------------------

    @Test
    void addFavouriteDesk_ShouldSave_WhenNotAlreadyFavourite() {
        when(favouriteDesksRepository.findByUserIdAndDeskId(2L, 2L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(deskRepository.findById(2L)).thenReturn(Optional.of(desk2));

        favouriteDesksService.addFavouriteDesk(2L, 2L);

        ArgumentCaptor<FavouriteDesks> captor = ArgumentCaptor.forClass(FavouriteDesks.class);
        verify(favouriteDesksRepository).save(captor.capture());

        FavouriteDesks saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user2);
        assertThat(saved.getDesk()).isEqualTo(desk2);
    }

    @Test
    void addFavouriteDesk_ShouldNotSave_WhenAlreadyFavourite() {
        FavouriteDesks existingFav = new FavouriteDesks(user2, desk2);

        when(favouriteDesksRepository.findByUserIdAndDeskId(2L, 2L))
                .thenReturn(Optional.of(existingFav));

        favouriteDesksService.addFavouriteDesk(2L, 2L);

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void addFavouriteDesk_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteDesksService.addFavouriteDesk(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void addFavouriteDesk_ShouldThrowException_WhenDeskNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(deskRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteDesksService.addFavouriteDesk(2L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desk not found");

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void removeFavouriteDesk_ShouldDeleteCorrectly() {
        favouriteDesksService.removeFavouriteDesk(1L, 3L);
        verify(favouriteDesksRepository).deleteByUserIdAndDeskId(1L, 3L);
    }
    

    @Test
    void getFavouriteDesksDTO_ShouldReturnMappedDTOs() {
        FavouriteDesks fav = new FavouriteDesks(user1, desk3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(favouriteDesksRepository.findByUser(user1)).thenReturn(List.of(fav));

        List<FavouriteDesksDTO> result = favouriteDesksService.getFavouriteDesksDTO(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeskName()).isEqualTo("PLC 201");
        assertThat(result.get(0).getZone()).isEqualTo("PLC");
        assertThat(result.get(0).isFavourite()).isTrue();

        verify(favouriteDesksRepository, times(1)).findByUser(user1);
    }

    @Test
    void getAllDesksWithFavourites_ShouldReturnAllDesks_WithCorrectFavouriteFlags() {
        FavouriteDesks fav = new FavouriteDesks(user1, desk2);

        when(deskRepository.findAll()).thenReturn(List.of(desk2, desk3));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(favouriteDesksRepository.findByUser(user1)).thenReturn(List.of(fav));

        List<FavouriteDesksDTO> result = favouriteDesksService.getAllDesksWithFavourites(1L);

        assertThat(result).hasSize(2);

        FavouriteDesksDTO desk2Dto = result.stream()
                .filter(dto -> dto.getDeskId().equals(2L))
                .findFirst()
                .orElseThrow();

        FavouriteDesksDTO desk3Dto = result.stream()
                .filter(dto -> dto.getDeskId().equals(3L))
                .findFirst()
                .orElseThrow();

        assertThat(desk2Dto.isFavourite()).isTrue();
        assertThat(desk3Dto.isFavourite()).isFalse();

        verify(deskRepository, times(1)).findAll();
        verify(favouriteDesksRepository, times(1)).findByUser(user1);
    }
}
