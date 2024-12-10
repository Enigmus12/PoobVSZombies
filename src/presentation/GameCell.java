package presentation;

import domain.Board;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameCell extends JButton {
    private GameCell previous;
    private GameCell next;
    private int row;
    private int column;
    private Board board;
    private String currentPlantType;
    private String currentZombieType;
    private ImageIcon backgroundImage;
    private int bgX, bgY;
    private Timer moveTimer;
    private boolean occuped;
    private boolean haveZombie;
    private Timer peaTimer;
    private List<Pea> peas; // Lista para almacenar las "peas"

    private static final Map<String, String> PLANT_IMAGES = Map.of(
            "SunFlower", "images/SunFlower.png",
            "PeasShooter", "images/PeasShooter.png",
            "WallNut", "images/WallNut.png",
            "PotatoMine", "images/PotatoMine.png",
            "EciPlant", "images/EciPlant.png"
    );

    private static final Map<String, String> ZOMBIE_IMAGES = Map.of(
            "ZombieBasic", "images/ZombieBasic.png"
    );

    public GameCell(int row, int column) {
        super();
        this.row = row;
        this.column = column;
        this.occuped = false;
        this.currentPlantType = null;
        this.currentZombieType = null;
        this.bgX = 0;
        this.bgY = 0;
        this.peas = new ArrayList<>();
        setContentAreaFilled(false);
        haveZombie = false;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public boolean isOccupied() {
        return occuped;
    }

    public boolean hasZombie() {
        return haveZombie;
    }

    public void setBackgroundImage(String imagePath) {
        if (imagePath != null) {
            this.backgroundImage = new ImageIcon(imagePath);
        } else {
            this.backgroundImage = null;
        }
        repaint();
    }

    public void setPrevious(GameCell previous) {
        this.previous = previous;
    }

    public void setNext(GameCell next) {
        this.next = next;
    }

    public void addPlant(String plantType) {
        this.currentPlantType = plantType;
        if (plantType.equals("PeasShooter")) {
            startPeaTimer();
        }
        updateBackgroundImage(PLANT_IMAGES.get(plantType));
        occuped = true;
    }

    public void removePlant() {
        if (currentPlantType != null) {
            if (peaTimer != null) {
                peaTimer.stop();
            }
            this.currentPlantType = null;
            this.backgroundImage = null;
            repaint();
        }
    }

    public void addZombie(String zombieType) {
        this.currentZombieType = zombieType;
        updateBackgroundImage(ZOMBIE_IMAGES.getOrDefault(zombieType, ZOMBIE_IMAGES.get("ZombieBasic")));
        initializeZombieMovement();
        occuped = true;
        haveZombie = true;
    }

    private void updateBackgroundImage(String imagePath) {
        if (imagePath != null) {
            this.backgroundImage = new ImageIcon(imagePath);
        } else {
            this.backgroundImage = null;
        }
        repaint();
    }

    private void startPeaTimer() {
        peaTimer = new Timer(5000, e -> addPea());
        peaTimer.start();
    }

    private void addPea() {
        Pea newPea = new Pea(0, getHeight() / 2 - 10);
        peas.add(newPea);
        newPea.startMovement();
    }

    private void initializeZombieMovement() {
        moveTimer = new Timer(100, e -> {
            bgX -= 1;
            if (bgX < -getWidth()) {
                ((Timer) e.getSource()).stop();
                send("Zombie", currentZombieType);
                removeBackground();
            }
            repaint();
        });
        moveTimer.start();
    }

    public void receive(String type, String typeCharacter) {
        if ("Zombie".equals(type)) {
            if (!isOccupied()) {
                addZombie(typeCharacter);
            } else {
                System.out.println("Cell occupied, cannot receive zombie");
            }
        } else if ("Pea".equals(type)) {
            handlePea();
        }
    }

    private void handlePea() {
        if (isOccupied() && haveZombie) {
            // Detener "pea" y continuar con el zombi
            System.out.println("The 'Pea' stops and disappears.");
        } else {
            System.out.println("The 'Pea' passes through an empty cell.");
        }
    }

    private void send(String type, String typeCharacter) {
        if ("Pea".equals(type)) {
            sendPea();
        } else if ("Zombie".equals(type)) {
            sendZombie();
        }
    }

    private void sendPea() {
        if (next != null) {
            next.receive("Pea", "Pea");
            next.addPea();
        }
    }

    private void sendZombie() {
        if (previous != null) {
            previous.receive("Zombie", currentZombieType);
        }
    }

    public void removeBackground() {
        this.backgroundImage = null;
        repaint();
    }

    public void addLawnMower(int row) {
        setBackgroundImage("images/Mower.png");
        occuped = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage.getImage(), bgX, bgY, getWidth(), getHeight(), this);
        }
        for (Pea pea : peas) {
            g2d.drawImage(new ImageIcon("images/Pea.png").getImage(), pea.x, 0, getWidth(), getHeight(), this);
        }
    }

    private class Pea {
        int x, y;
        private Timer movePeaTimer;

        Pea(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void startMovement() {
            movePeaTimer = new Timer(50, e -> {
                x += 2;
                if (x > getWidth()) {
                    send("Pea", "Pea");
                    peas.remove(this);
                    movePeaTimer.stop();
                } else if (hasZombie() && x >= bgX) {
                    // Detener "pea" y continuar con el zombi
                    peas.remove(this);
                    movePeaTimer.stop();
                    repaint();
                }
                repaint();
            });
            movePeaTimer.start();
        }
    }
}
