package com.stupidpeople.cuentanos.ui;

public abstract class UiGeneric implements NotificationInterface {

    ActionsInterface actionsInterface;


    public UiGeneric(ActionsInterface actionsInterface) {
        this.actionsInterface = actionsInterface;
    }

    public void apretado(TipoBoton tipoBoton) {
        switch (tipoBoton) {
            case PLAY:
                actionsInterface.apretadoPlay();
                break;
            case PAUSE:
                actionsInterface.apretadoPause();
                break;
            case NEXT:
                actionsInterface.apretadoNextOrHate();
                break;
            case PREV:
                actionsInterface.apretadoLikeOrBack();
                break;
        }
    }

    private enum TipoBoton {
        PLAY, PAUSE, NEXT, PREV
    }
}
