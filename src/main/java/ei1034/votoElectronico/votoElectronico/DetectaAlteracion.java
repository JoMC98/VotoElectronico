package ei1034.votoElectronico.votoElectronico;

public class DetectaAlteracion {
    private boolean hayAlteracion;

    public DetectaAlteracion() {
        hayAlteracion = false;
    }

    synchronized public void nuevaAlteracion() {
        hayAlteracion = true;
    }

    synchronized public boolean getEstado() {
        return hayAlteracion;
    }
}
