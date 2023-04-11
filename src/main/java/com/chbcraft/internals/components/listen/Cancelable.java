package com.chbcraft.internals.components.listen;

public interface Cancelable {
    public void setCancel();
    public void enableEvent();
    public boolean isCancel();
}
