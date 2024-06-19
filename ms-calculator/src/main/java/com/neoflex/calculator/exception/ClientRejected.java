package com.neoflex.calculator.exception;

/*
 *ClientRejected
 *
 * @author Shilin Vyacheslav
 */
public class ClientRejected extends RuntimeException{
    public ClientRejected(String message){
        super(message);
    }
}
