package com.project.internship_desk_booking_system;

import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
import com.project.internship_desk_booking_system.entity.Desk;
import com.project.internship_desk_booking_system.entity.FavouriteDesks;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.entity.Zone;
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

        Zone zoneServ=new Zone();
        zoneServ.setId(1L);
        zoneServ.setZoneName("Service");
        zoneServ.setZoneAbv("SER");

        Zone zonePLC = new Zone();
        zonePLC.setId(2L);
        zonePLC.setZoneName("PLC");
        zonePLC.setZoneAbv("PLC");


        desk2 = new Desk();
        desk2.setId(2L);
        desk2.setDeskName("Serv 101");
        desk2.setZone(zoneServ);
        desk2.setType(DeskType.SHARED);
        desk2.setStatus(DeskStatus.ACTIVE);
        desk2.setIsTemporarilyAvailable(true);
        desk2.setTemporaryAvailableFrom(LocalDateTime.now());
        desk2.setTemporaryAvailableUntil(LocalDateTime.now().plusDays(5));

        desk3 = new Desk();
        desk3.setId(3L);
        desk3.setDeskName("PLC 201");
        desk3.setZone(zonePLC);
        desk3.setType(DeskType.SHARED);
        desk3.setStatus(DeskStatus.ACTIVE);
        desk3.setIsTemporarilyAvailable(true);
        desk3.setTemporaryAvailableFrom(LocalDateTime.now());
        desk3.setTemporaryAvailableUntil(LocalDateTime.now().plusDays(5));
    }

    @Test
    void addFavouriteDesk_ShouldSave_WhenNotAlreadyFavourite() {
        String email = "ion.paun@gmail.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user2));
        when(favouriteDesksRepository.findByUserIdAndDeskId(user2.getId(), 2L)).thenReturn(Optional.empty());
        when(deskRepository.findById(2L)).thenReturn(Optional.of(desk2));

        favouriteDesksService.addFavouriteDesk(email, 2L);

        ArgumentCaptor<FavouriteDesks> captor = ArgumentCaptor.forClass(FavouriteDesks.class);
        verify(favouriteDesksRepository).save(captor.capture());

        FavouriteDesks saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user2);
        assertThat(saved.getDesk()).isEqualTo(desk2);
    }

    @Test
    void addFavouriteDesk_ShouldNotSave_WhenAlreadyFavourite() {
        String email = "ion.paun@gmail.com";
        FavouriteDesks existingFav = new FavouriteDesks(user2, desk2);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user2));
        when(favouriteDesksRepository.findByUserIdAndDeskId(user2.getId(), 2L)).thenReturn(Optional.of(existingFav));

        favouriteDesksService.addFavouriteDesk(email, 2L);

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void addFavouriteDesk_ShouldThrowException_WhenUserNotFound() {
        String email = "unknown@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteDesksService.addFavouriteDesk(email, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void addFavouriteDesk_ShouldThrowException_WhenDeskNotFound() {
        String email = "ion.paun@gmail.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user2));
        when(deskRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteDesksService.addFavouriteDesk(email, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Desk not found");

        verify(favouriteDesksRepository, never()).save(any());
    }

    @Test
    void removeFavouriteDesk_ShouldDeleteCorrectly() {
        String email = "maria.florea@gmail.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user1));

        favouriteDesksService.removeFavouriteDesk(email, 3L);

        verify(favouriteDesksRepository).deleteByUserIdAndDeskId(user1.getId(), 3L);
    }

    @Test
    void getFavouriteDesksDTO_ShouldReturnMappedDTOs() {
        String email = "maria.florea@gmail.com";
        FavouriteDesks fav = new FavouriteDesks(user1, desk3);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user1));
        when(favouriteDesksRepository.findByUser(user1)).thenReturn(List.of(fav));

        List<FavouriteDesksDTO> result = favouriteDesksService.getFavouriteDesksDTO(email);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeskName()).isEqualTo("PLC 201");
        assertThat(result.get(0).getZone()).isEqualTo("PLC");
        assertThat(result.get(0).isFavourite()).isTrue();

        verify(favouriteDesksRepository, times(1)).findByUser(user1);
    }

    @Test
    void getAllDesksWithFavourites_ShouldReturnAllDesks_WithCorrectFavouriteFlags() {
        String email = "maria.florea@gmail.com";
        FavouriteDesks fav = new FavouriteDesks(user1, desk2);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user1));
        when(deskRepository.findAll()).thenReturn(List.of(desk2, desk3));
        when(favouriteDesksRepository.findByUser(user1)).thenReturn(List.of(fav));

        List<FavouriteDesksDTO> result = favouriteDesksService.getAllDesksWithFavourites(email);

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
