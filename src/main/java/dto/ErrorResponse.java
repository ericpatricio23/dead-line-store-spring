package dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private int status;
    private String erro;
    private LocalDateTime data;

    public ErrorResponse(int status, String erro, LocalDateTime data){
        this.status = status;
        this.erro = erro;
        this.data = data;
    }


    public ErrorResponse(LocalDateTime now, int i, String message) {
    }
}
