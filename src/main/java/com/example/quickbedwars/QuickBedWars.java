package com.example.quickbedwars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

@Mod(
        modid = "quickbedwars",
        name = "QuickBedWars",
        version = "6.0",
        clientSideOnly = true
)
public class QuickBedWars {

    private final Minecraft mc = Minecraft.getMinecraft();

    private KeyBinding key;

    private boolean waitingForGui = false;
    private boolean clicked = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        key = new KeyBinding(
                "Quick BedWars NPC",
                Keyboard.KEY_L,
                "QuickBedWars"
        );

        ClientRegistry.registerKeyBinding(key);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {

        if (!key.isPressed())
            return;

        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (waitingForGui)
            return;

        EntityPlayer npc = findNearestPlayer();

        if (npc == null) {
            mc.thePlayer.addChatMessage(
                    new ChatComponentText(
                            "§c[QuickBedWars] No NPC nearby."
                    )
            );
            return;
        }

        // Turn toward the nearest NPC
        faceEntity(npc);

        // Right-click the NPC
        mc.playerController.interactWithEntitySendPacket(
                mc.thePlayer,
                npc
        );

        // Wait for the GUI to open
        waitingForGui = true;
        clicked = false;
    }

    private EntityPlayer findNearestPlayer() {

        EntityPlayer closest = null;
        double closestDistance = 36.0D; // 6 blocks

        List<EntityPlayer> players =
                mc.theWorld.playerEntities;

        for (EntityPlayer player : players) {

            if (player == mc.thePlayer)
                continue;

            double distance =
                    mc.thePlayer.getDistanceSqToEntity(player);

            if (distance < closestDistance) {
                closestDistance = distance;
                closest = player;
            }
        }

        return closest;
    }

    private void faceEntity(Entity entity) {

        double x =
                entity.posX - mc.thePlayer.posX;

        double y =
                entity.posY +
                entity.getEyeHeight() -
                (mc.thePlayer.posY +
                mc.thePlayer.getEyeHeight());

        double z =
                entity.posZ - mc.thePlayer.posZ;

        double horizontal =
                Math.sqrt(x * x + z * z);

        float yaw =
                (float)
                (Math.atan2(z, x)
                * 180.0D / Math.PI)
                - 90.0F;

        float pitch =
                (float)
                -(Math.atan2(y, horizontal)
                * 180.0D / Math.PI);

        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END)
            return;

        if (!waitingForGui)
            return;

        if (mc.thePlayer == null)
            return;

        if (clicked)
            return;

        // Wait until the NPC opens an inventory GUI
        if (!(mc.thePlayer.openContainer instanceof ContainerChest))
            return;

        ContainerChest chest =
                (ContainerChest) mc.thePlayer.openContainer;

        /*
         * First green BedWars button:
         * Slot 11
         */

        if (chest.inventorySlots.size() > 11) {

            mc.playerController.windowClick(
                    chest.windowId,
                    11,
                    0,
                    0,
                    mc.thePlayer
            );

            clicked = true;
            waitingForGui = false;
        }
    }
}
