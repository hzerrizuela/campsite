package com.campsite.interview.service;

import com.campsite.interview.entity.ConfigurationEntity;
import com.campsite.interview.entity.ReservationEntity;
import com.campsite.interview.exception.DataLayerException;
import com.campsite.interview.repository.ConfigRepository;
import com.campsite.interview.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.campsite.interview.constant.ErrorConstants.DATA_LAYER_ERROR;

@Service
@Slf4j
public class PersistenceHelper {

    private ReservationRepository reservationRepository;
    private ConfigRepository configRepository;

    private PersistenceHelper(ConfigRepository configRepository, ReservationRepository reservationRepository) {
        this.configRepository = configRepository;
        this.reservationRepository = reservationRepository;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceHelper.class);

    public ConfigurationEntity saveConfiguration(ConfigurationEntity entity) {
        try {
            LOGGER.info("Saving entity: {}", entity);
            return configRepository.save(entity);
        } catch (Exception e) {
            LOGGER.error(DATA_LAYER_ERROR, e);
            throw new DataLayerException(DATA_LAYER_ERROR);
        }
    }

    public ReservationEntity saveReservation(ReservationEntity entity) {
        try {
            LOGGER.info("Saving entity: {}", entity);
            return reservationRepository.save(entity);
        } catch (Exception e) {
            LOGGER.error(DATA_LAYER_ERROR, e);
            throw new DataLayerException(DATA_LAYER_ERROR);
        }
    }
}
