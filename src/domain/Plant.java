package domain;

public abstract class Plant extends Character {
    protected int rechargeTime;
    protected int sunCost;
    
    /**
     * Verifica si la planta puede ser plantada
     * @return true si cumple las condiciones para ser plantada
     */
    public boolean canBePlanted() {
        // Lógica específica para verificar si se puede plantar
        return true;
    }
    
    public int getSunCost() {
        return sunCost;
    }
    
}