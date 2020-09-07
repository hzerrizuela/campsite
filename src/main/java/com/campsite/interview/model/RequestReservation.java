package com.campsite.interview.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;


@ApiModel
public class RequestReservation implements Serializable {

    @Pattern(regexp = "^[A-Za-z]+$")
    private String name;
    @Pattern(regexp = "^[A-Za-z]+$")
    private String lastname;
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String arrivalDate;
    @NotEmpty
    private String departureDate;

    public RequestReservation() {
    }

    @ApiModelProperty(required = true)
    public String getName() {
        return name;
    }

    @ApiModelProperty(example = "2020-10-01")
    public String getArrivalDate() {
        return arrivalDate;
    }

    @ApiModelProperty(required = true)
    public String getLastname() {
        return lastname;
    }

    @ApiModelProperty(required = true, example = "joe@yopmail.com")
    public String getEmail() {
        return email;
    }

    @ApiModelProperty(example = "2020-10-02")
    public String getDepartureDate() {
        return departureDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

}
