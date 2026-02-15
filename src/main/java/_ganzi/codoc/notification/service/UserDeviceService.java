package _ganzi.codoc.notification.service;

import _ganzi.codoc.notification.domain.UserDevice;
import _ganzi.codoc.notification.enums.DevicePlatform;
import _ganzi.codoc.notification.repository.UserDeviceRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserDeviceService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Transactional
    public void registerDevice(Long userId, DevicePlatform platform, String pushToken) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        userDeviceRepository
                .findByUserId(userId)
                .ifPresentOrElse(
                        device -> device.updateRegistration(user, platform, pushToken),
                        () -> userDeviceRepository.save(UserDevice.create(user, platform, pushToken)));
    }

    @Transactional
    public void deactivateDevice(Long userId) {
        userDeviceRepository.findByUserId(userId).ifPresent(UserDevice::deactivate);
    }
}
