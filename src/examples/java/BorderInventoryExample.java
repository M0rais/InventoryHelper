import br.com.blecaute.inventory.InventoryBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BorderInventoryExample {

    private final Random random = new Random();

    // quantidade de linhas do inventário
    private final int inventorySize = 6;

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        List<ItemStack> border = new ArrayList<>();

        // pegando a quantidade de itens na borda
        int itemsSize = inventorySize <= 2 ? 0 : inventorySize * 9 - (inventorySize - 2) * 7;
        // criando os itens da borda
        for (int index = 0; index < itemsSize; index++) {
            border.add(getRandomGlass());
        }

        // construindo o inventário
        new InventoryBuilder<>("Random border", inventorySize)
                // definindo a function que irá verificar se o slot é inválido
                .withSkip(slot -> !isBorder(inventorySize, slot))
                // definindo os items
                .withItems(border, click -> event.getPlayer().sendMessage("§cVocê clicou em uma borda!"))
                // construindo o inventário e abrindo para o jogador
                .build(event.getPlayer());

    }

    public static boolean isBorder(int size, int slot) {
        // caso o inventário tiver apenas 2 linha ou menos ele não terá borda
        if (size <= 2) {
            return false;
        }

        // verificando se o slot esa na borda superior
        if (slot >= 0 && slot <= 8) {
            return true;
        }

        // verificando se o slot está na borda inferior
        int rows = size * 9;
        if (slot >= rows - 9 && slot < rows) {
            return true;
        }

        // pegando a quantidade de linhas restantes
        int lines = size - 1;
        for (int index = 1; index < lines; index++) {
            // pegando a borda direita
            int rightBorder = index * 9;
            // pegando a borda esquerda
            int leftBorder = rightBorder + 8;
            // comparando o slot com as bordas
            if (slot == rightBorder || slot == leftBorder) {
                return true;
            }
        }

        return false;
    }

    // Método para gerar um vidro colorido aleatório
    public ItemStack getRandomGlass() {
        // gerando uma data aleatória
        short data = (short) random.nextInt(16);
        // criando o ItemStack
        ItemStack itemStack = new ItemStack(Material.STAINED_GLASS_PANE);
        // definindo a data pela durabilidade
        itemStack.setDurability(data);

        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName("§eBorda");

        itemStack.setItemMeta(meta);

        return itemStack;
    }
}