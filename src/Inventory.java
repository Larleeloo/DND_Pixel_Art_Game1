import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Manages collected items and displays inventory UI
 */
class Inventory {

    private ArrayList<ItemEntity> items;
    private int maxSlots;
    private boolean isOpen;

    // UI positioning
    private int uiX, uiY;
    private int slotSize;
    private int padding;

    // Drag and drop
    private ItemEntity draggedItem;
    private int draggedIndex;
    private int dragX, dragY;
    private boolean isDragging;

    public Inventory(int maxSlots) {
        this.items = new ArrayList<>();
        this.maxSlots = maxSlots;
        this.isOpen = false;

        // UI settings
        this.slotSize = 60;
        this.padding = 10;
        this.uiX = 50;
        this.uiY = 80;

        // Drag state
        this.draggedItem = null;
        this.draggedIndex = -1;
        this.isDragging = false;
    }

    public boolean addItem(ItemEntity item) {
        if (items.size() < maxSlots) {
            items.add(item);
            System.out.println("Added to inventory: " + item.getItemName() + " (" + (items.size()) + "/" + maxSlots + ")");
            return true;
        }
        System.out.println("Inventory full!");
        return false;
    }

    public void toggleOpen() {
        isOpen = !isOpen;
        System.out.println("Inventory " + (isOpen ? "opened" : "closed"));
    }

    public boolean isOpen() {
        return isOpen;
    }

    public int getItemCount() {
        return items.size();
    }

    public void handleMousePressed(int mouseX, int mouseY) {
        if (!isOpen) return;

        int cols = 5;
        int panelX = (1920 - (cols * (slotSize + padding) + padding)) / 2;
        int panelY = 150;

        // Check if clicking on an item slot
        for (int i = 0; i < items.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 50 + row * (slotSize + padding);

            if (mouseX >= slotX && mouseX <= slotX + slotSize &&
                    mouseY >= slotY && mouseY <= slotY + slotSize) {
                // Start dragging this item
                draggedItem = items.get(i);
                draggedIndex = i;
                isDragging = true;
                dragX = mouseX;
                dragY = mouseY;
                System.out.println("Started dragging: " + draggedItem.getItemName());
                break;
            }
        }
    }

    public void handleMouseDragged(int mouseX, int mouseY) {
        if (isDragging) {
            dragX = mouseX;
            dragY = mouseY;
        }
    }

    public ItemEntity handleMouseReleased(int mouseX, int mouseY) {
        if (!isDragging) return null;

        ItemEntity droppedItem = null;

        // Check if dropped outside inventory panel
        int cols = 5;
        int rows = (int)Math.ceil(maxSlots / (double)cols);
        int panelWidth = cols * (slotSize + padding) + padding;
        int panelHeight = rows * (slotSize + padding) + padding + 50;
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        boolean outsideInventory = mouseX < panelX || mouseX > panelX + panelWidth ||
                mouseY < panelY || mouseY > panelY + panelHeight;

        if (outsideInventory && draggedItem != null) {
            // Drop the item into the world
            droppedItem = draggedItem;
            items.remove(draggedIndex);
            System.out.println("Dropped item outside inventory: " + droppedItem.getItemName());
        } else {
            System.out.println("Released item inside inventory (not dropping)");
        }

        // Reset drag state
        draggedItem = null;
        draggedIndex = -1;
        isDragging = false;

        return droppedItem;
    }

    public boolean removeItem(ItemEntity item) {
        return items.remove(item);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isOpen) {
            // Draw full inventory panel
            drawFullInventory(g2d);

            // Draw dragged item on top if dragging
            if (isDragging && draggedItem != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                if (draggedItem.getSprite() != null) {
                    g2d.drawImage(draggedItem.getSprite(),
                            dragX - slotSize/2, dragY - slotSize/2,
                            slotSize, slotSize, null);
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        } else {
            // Draw compact inventory preview (always visible)
            drawCompactInventory(g2d);
        }
    }

    private void drawCompactInventory(Graphics2D g2d) {
        // Small bar showing item count
        int barWidth = 200;
        int barHeight = 40;
        int barX = 10;
        int barY = 80;

        // Background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        // Border
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 10, 10);

        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String text = "Items: " + items.size() + "/" + maxSlots + " [I]";
        g2d.drawString(text, barX + 15, barY + 26);

        // Draw first few items as icons
        int iconSize = 30;
        int iconX = barX + 10;
        int iconY = barY + barHeight + 5;

        for (int i = 0; i < Math.min(3, items.size()); i++) {
            ItemEntity item = items.get(i);
            if (item.getSprite() != null) {
                g2d.drawImage(item.getSprite(), iconX + (i * (iconSize + 5)), iconY, iconSize, iconSize, null);
            }
        }
    }

    private void drawFullInventory(Graphics2D g2d) {
        int cols = 5;
        int rows = (int)Math.ceil(maxSlots / (double)cols);
        int panelWidth = cols * (slotSize + padding) + padding;
        int panelHeight = rows * (slotSize + padding) + padding + 50;

        // Center the panel
        int panelX = (1920 - panelWidth) / 2;
        int panelY = 150;

        // Background panel
        g2d.setColor(new Color(40, 40, 40, 230));
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Border
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("INVENTORY", panelX + 20, panelY + 35);

        // Draw slots
        for (int i = 0; i < maxSlots; i++) {
            int col = i % cols;
            int row = i / cols;
            int slotX = panelX + padding + col * (slotSize + padding);
            int slotY = panelY + 50 + row * (slotSize + padding);

            // Slot background
            if (i < items.size()) {
                g2d.setColor(new Color(80, 80, 120, 200));
            } else {
                g2d.setColor(new Color(60, 60, 60, 200));
            }
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Slot border
            g2d.setColor(new Color(150, 150, 150));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Draw item if present
            if (i < items.size()) {
                ItemEntity item = items.get(i);

                // Skip drawing if this is the item being dragged
                if (isDragging && draggedIndex == i) {
                    // Draw empty slot with dashed border
                    g2d.setColor(new Color(100, 100, 100, 100));
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                            BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                    g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);
                } else {
                    // Draw normal item
                    if (item.getSprite() != null) {
                        g2d.drawImage(item.getSprite(), slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, null);
                    }

                    // Item name
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = item.getItemName();
                    if (fm.stringWidth(name) > slotSize) {
                        name = name.substring(0, 6) + "...";
                    }
                    int textX = slotX + (slotSize - fm.stringWidth(name)) / 2;
                    g2d.drawString(name, textX, slotY + slotSize + 15);
                }
            }
        }

        // Instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 14));
        g2d.drawString("Press [I] to close | Drag items outside to drop", panelX + 20, panelY + panelHeight - 15);
    }
}