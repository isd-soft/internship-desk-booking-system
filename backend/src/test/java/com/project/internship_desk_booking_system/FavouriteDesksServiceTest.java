//package com.project.internship_desk_booking_system;
//
//import com.project.internship_desk_booking_system.dto.FavouriteDesksDTO;
//import com.project.internship_desk_booking_system.entity.Desk;
//import com.project.internship_desk_booking_system.entity.FavouriteDesks;
//import com.project.internship_desk_booking_system.entity.User;
//import com.project.internship_desk_booking_system.error.ExceptionResponse;
//import com.project.internship_desk_booking_system.mapper.FavouriteDeskMapper;
//import com.project.internship_desk_booking_system.repository.DeskRepository;
//import com.project.internship_desk_booking_system.repository.FavouriteDesksRepository;
//import com.project.internship_desk_booking_system.repository.UserRepository;
//import com.project.internship_desk_booking_system.service.FavouriteDesksService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.http.HttpStatus;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class FavouriteDesksServiceTest {
//
//    @Mock
//    private FavouriteDesksRepository favouriteDesksRepository;
//    @Mock
//    private DeskRepository deskRepository;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private FavouriteDeskMapper favouriteDeskMapper;
//
//    @InjectMocks
//    private FavouriteDesksService favouriteDesksService;
//
//    private User user;
//    private Desk desk;
//    private FavouriteDesks favourite;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        user = new User();
//        user.setId(1L);
//        user.setEmail("john@example.com");
//
//        desk = new Desk();
//        desk.setId(10L);
//        desk.setDeskName("Desk 10");
//
//        favourite = new FavouriteDesks(user, desk);
//    }
//
//    @Test
//    void addFavouriteDesk_ShouldSave_WhenNotAlreadyFavourite() {
//        when(userRepository.findByEmailIgnoreCase("john@example.com"))
//                .thenReturn(Optional.of(user));
//        when(favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), desk.getId()))
//                .thenReturn(Optional.empty());
//        when(deskRepository.findById(desk.getId()))
//                .thenReturn(Optional.of(desk));
//
//        favouriteDesksService.addFavouriteDesk("john@example.com", desk.getId());
//
//        ArgumentCaptor<FavouriteDesks> captor = ArgumentCaptor.forClass(FavouriteDesks.class);
//        verify(favouriteDesksRepository).save(captor.capture());
//
//        FavouriteDesks saved = captor.getValue();
//        assertThat(saved.getUser()).isEqualTo(user);
//        assertThat(saved.getDesk()).isEqualTo(desk);
//    }
//
//    @Test
//    void addFavouriteDesk_ShouldNotSave_WhenAlreadyFavourite() {
//        when(userRepository.findByEmailIgnoreCase("john@example.com"))
//                .thenReturn(Optional.of(user));
//        when(favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), desk.getId()))
//                .thenReturn(Optional.of(favourite));
//
//        favouriteDesksService.addFavouriteDesk("john@example.com", desk.getId());
//
//        verify(favouriteDesksRepository, never()).save(any());
//        verify(deskRepository, never()).findById(any());
//    }
//
//    @Test
//    void addFavouriteDesk_ShouldThrow_WhenUserNotFound() {
//        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
//                .thenReturn(Optional.empty());
//
//        ExceptionResponse ex = catchThrowableOfType(
//                () -> favouriteDesksService.addFavouriteDesk("unknown@example.com", desk.getId()),
//                ExceptionResponse.class
//        );
//
//        assertThat(ex.getStatusOverride()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND");
//        assertThat(ex.getMessage()).isEqualTo("user not found");
//    }
//
//    @Test
//    void addFavouriteDesk_ShouldThrow_WhenDeskNotFound() {
//        when(userRepository.findByEmailIgnoreCase("john@example.com"))
//                .thenReturn(Optional.of(user));
//        when(favouriteDesksRepository.findByUserIdAndDeskId(user.getId(), desk.getId()))
//                .thenReturn(Optional.empty());
//        when(deskRepository.findById(desk.getId()))
//                .thenReturn(Optional.empty());
//
//        ExceptionResponse ex = catchThrowableOfType(
//                () -> favouriteDesksService.addFavouriteDesk("john@example.com", desk.getId()),
//                ExceptionResponse.class
//        );
//
//        assertThat(ex.getStatusOverride()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(ex.getCode()).isEqualTo("DESK NOT_FOUND");
//        assertThat(ex.getMessage()).isEqualTo("desk not found");
//    }
//
//    @Test
//    void removeFavouriteDesk_ShouldDeleteByUserIdAndDeskId() {
//        when(userRepository.findByEmailIgnoreCase("john@example.com"))
//                .thenReturn(Optional.of(user));
//
//        favouriteDesksService.removeFavouriteDesk("john@example.com", desk.getId());
//
//        verify(favouriteDesksRepository).deleteByUserIdAndDeskId(user.getId(), desk.getId());
//    }
//
//    @Test
//    void removeFavouriteDesk_ShouldThrow_WhenUserNotFound() {
//        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
//                .thenReturn(Optional.empty());
//
//        ExceptionResponse ex = catchThrowableOfType(
//                () -> favouriteDesksService.removeFavouriteDesk("unknown@example.com", 10L),
//                ExceptionResponse.class
//        );
//
//        assertThat(ex.getStatusOverride()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND");
//        assertThat(ex.getMessage()).isEqualTo("user not found");
//    }
//
//    @Test
//    void getFavouriteDesks_ShouldReturnMappedDtos() {
//        FavouriteDesksDTO dto = new FavouriteDesksDTO();
//        dto.setDeskId(10L);
//        dto.setDeskName("Desk 10");
//
//        when(userRepository.findByEmailIgnoreCase("john@example.com"))
//                .thenReturn(Optional.of(user));
//        when(favouriteDesksRepository.findByUser(user))
//                .thenReturn(List.of(favourite));
//        when(favouriteDeskMapper.toDto(favourite))
//                .thenReturn(dto);
//
//        List<FavouriteDesksDTO> result = favouriteDesksService.getFavouriteDesks("john@example.com");
//
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).getDeskId()).isEqualTo(10L);
//        assertThat(result.get(0).getDeskName()).isEqualTo("Desk 10");
//
//        verify(favouriteDesksRepository).findByUser(user);
//        verify(favouriteDeskMapper).toDto(favourite);
//    }
//
//    @Test
//    void getFavouriteDesks_ShouldThrow_WhenUserNotFound() {
//        when(userRepository.findByEmailIgnoreCase("unknown@example.com"))
//                .thenReturn(Optional.empty());
//
//        ExceptionResponse ex = catchThrowableOfType(
//                () -> favouriteDesksService.getFavouriteDesks("unknown@example.com"),
//                ExceptionResponse.class
//        );
//
//        assertThat(ex.getStatusOverride()).isEqualTo(HttpStatus.NOT_FOUND);
//        assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND");
//        assertThat(ex.getMessage()).isEqualTo("user not found");
//    }
//}
