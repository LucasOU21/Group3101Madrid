package com.example.group3101madrid.Perfil;

public class Desafio {
    private String type;
    private Long startDate;

    public Desafio(String type, Long startDate) {
        this.type = type;
        this.startDate = startDate;
    }

    public String getType() { return type; }
    public Long getStartDate() { return startDate; }
}
