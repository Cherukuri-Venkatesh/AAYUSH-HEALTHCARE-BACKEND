package com.project.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.dto.DoctorDTO;
import com.project.entity.Admindoctor;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        // Important: avoid startup failure on ambiguous fields
        mapper.getConfiguration().setAmbiguityIgnored(true);

        // Keep explicit skip for ambiguous destination fields
        mapper.createTypeMap(Admindoctor.class, DoctorDTO.class)
              .addMappings(m -> {
                  m.skip(DoctorDTO::setHospitalId);
                  m.skip(DoctorDTO::setHospitalName);
              });

        return mapper;
    }
}