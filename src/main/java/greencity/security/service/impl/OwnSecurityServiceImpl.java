package greencity.security.service.impl;

import static greencity.constant.ErrorMessage.*;

import greencity.entity.OwnSecurity;
import greencity.entity.User;
import greencity.entity.enums.EmailNotification;
import greencity.entity.enums.ROLE;
import greencity.entity.enums.UserStatus;
import greencity.exception.*;
import greencity.security.dto.AccessTokenDto;
import greencity.security.dto.SuccessSignInDto;
import greencity.security.dto.ownsecurity.OwnSignInDto;
import greencity.security.dto.ownsecurity.OwnSignUpDto;
import greencity.security.dto.ownsecurity.UpdatePasswordDto;
import greencity.security.jwt.JwtTool;
import greencity.security.repository.OwnSecurityRepo;
import greencity.security.service.OwnSecurityService;
import greencity.security.service.VerifyEmailService;
import greencity.service.UserService;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@inheritDoc}
 */
@Service
@AllArgsConstructor
@Slf4j
public class OwnSecurityServiceImpl implements OwnSecurityService {
    private OwnSecurityRepo ownSecurityRepo;
    private UserService userService;
    private VerifyEmailService verifyEmailService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authManager;
    private JwtTool jwtTool;

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void signUp(OwnSignUpDto dto) {
        if (userService.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyRegisteredException(USER_ALREADY_REGISTERED_WITH_THIS_EMAIL);
        }
        User user = createNewRegisteredUser(dto);
        User savedUser = userService.save(user);
        ownSecurityRepo.save(createUserOwnSecurityToUser(dto, savedUser));
        verifyEmailService.save(savedUser);
    }

    private OwnSecurity createUserOwnSecurityToUser(OwnSignUpDto dto, User user) {
        return OwnSecurity.builder()
            .password(passwordEncoder.encode(dto.getPassword()))
            .user(user)
            .build();
    }

    private User createNewRegisteredUser(OwnSignUpDto dto) {
        return User.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .dateOfRegistration(LocalDateTime.now())
            .role(ROLE.ROLE_USER)
            .lastVisit(LocalDateTime.now())
            .userStatus(UserStatus.ACTIVATED)
            .emailNotification(EmailNotification.DISABLED)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(OwnSecurity userOwnSecurity) {
        if (!ownSecurityRepo.existsById(userOwnSecurity.getId())) {
            throw new BadIdException(NO_ENY_OWN_SECURITY_TO_DELETE + userOwnSecurity.getId());
        }
        ownSecurityRepo.delete(userOwnSecurity);
    }

    /**
     * {@inheritDoc}
     */
    @Scheduled(fixedRate = 86400000)
    @Override
    public void deleteNotActiveEmailUsers() { //TODO - do not drag all of the users
        verifyEmailService
            .findAll()
            .forEach(
                verifyEmail -> {
                    if (verifyEmailService.isDateValidate(verifyEmail.getExpiryDate())) {
                        delete(verifyEmail.getUser().getOwnSecurity());
                        verifyEmailService.delete(verifyEmail);
                        userService.deleteById(verifyEmail.getUser().getId());
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessSignInDto signIn(OwnSignInDto dto) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        User byEmail = userService
                .findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadEmailException(USER_NOT_FOUND_BY_EMAIL + dto.getEmail()));
        String accessToken = jwtTool.createAccessToken(byEmail.getEmail(), byEmail.getRole());
        String refreshToken = jwtTool.createRefreshToken(byEmail.getEmail());
        return new SuccessSignInDto(accessToken, refreshToken, byEmail.getFirstName(), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenDto updateAccessToken(String refreshToken) {
        if (jwtTool.isTokenValid(refreshToken)) {
            String email = jwtTool.getEmailByToken(refreshToken);
            User user = userService
                    .findByEmail(email)
                    .orElseThrow(() -> new BadEmailException(USER_NOT_FOUND_BY_EMAIL + email));
            return new AccessTokenDto(jwtTool.createAccessToken(user.getEmail(), user.getRole()));
        }
        throw new BadRefreshTokenException(REFRESH_TOKEN_NOT_VALID);
    }

    /**
     * {@inheritDoc}
     *
     * @author Dmytro Dovhal
     */
    @Override
    public void updatePassword(String pass, Long id) {
        String password = passwordEncoder.encode(pass);
        ownSecurityRepo.updatePassword(password, id);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateCurrentPassword(UpdatePasswordDto updatePasswordDto, String email) {
        log.info("Updating user password start");
        User user = userService
                .findByEmail(email)
                .orElseThrow(() -> new BadEmailException(USER_NOT_FOUND_BY_EMAIL + email));
        if (!updatePasswordDto.getPassword().equals(updatePasswordDto.getConfirmPassword())) {
            throw new PasswordsDoNotMatchesException(PASSWORDS_DO_NOT_MATCHES);
        }
        if (!passwordEncoder.matches(updatePasswordDto.getCurrentPassword(), user.getOwnSecurity().getPassword())) {
            throw new PasswordsDoNotMatchesException(PASSWORD_DOES_NOT_MATCH);
        }
        updatePassword(updatePasswordDto.getPassword(), user.getId());
        log.info("Updating user password end");
    }
}